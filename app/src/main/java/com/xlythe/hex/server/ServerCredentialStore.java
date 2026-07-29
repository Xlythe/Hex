package com.xlythe.hex.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Persists the reusable legacy login digest using an Android Keystore AES-GCM key.
 *
 * <p>The MD5 digest is password-equivalent to the old API, so it receives the
 * same protection as a plaintext password. The manifest backup rules exclude
 * this ciphertext because Android Keystore keys are device-bound.</p>
 */
public final class ServerCredentialStore {
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "hex.iggamecenter.credentials.v1";
    private static final String PREFERENCES = "iggamecenter_credentials";
    private static final String KEY_IV = "iv";
    private static final String KEY_PAYLOAD = "payload";
    private static final byte[] ASSOCIATED_DATA =
            "com.sam.hex:iggamecenter:v1".getBytes(java.nio.charset.StandardCharsets.UTF_8);

    private final SharedPreferences preferences;

    public ServerCredentialStore(Context context) {
        Context appContext = context.getApplicationContext();
        preferences = appContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public void save(ServerCredentials credentials) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        cipher.updateAAD(ASSOCIATED_DATA);
        byte[] ciphertext = cipher.doFinal(ServerCredentialCodec.encode(credentials));

        boolean saved = preferences.edit()
                .putString(KEY_IV, Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP))
                .putString(KEY_PAYLOAD, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
                .commit();
        if (!saved) throw new GeneralSecurityException("Could not persist server credentials");
    }

    /**
     * Returns null after logout, on a new device, or if the encrypted record was tampered with.
     * Corrupt ciphertext is removed so a future sign-in can recover normally.
     */
    public ServerCredentials load() {
        String encodedIv = preferences.getString(KEY_IV, null);
        String encodedPayload = preferences.getString(KEY_PAYLOAD, null);
        if (encodedIv == null || encodedPayload == null) {
            if (encodedIv != null || encodedPayload != null) clear();
            return null;
        }

        try {
            byte[] iv = Base64.decode(encodedIv, Base64.NO_WRAP);
            byte[] payload = Base64.decode(encodedPayload, Base64.NO_WRAP);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(128, iv));
            cipher.updateAAD(ASSOCIATED_DATA);
            return ServerCredentialCodec.decode(cipher.doFinal(payload));
        } catch (Exception e) {
            clear();
            return null;
        }
    }

    public void clear() {
        preferences.edit().clear().apply();
    }

    private static SecretKey getOrCreateKey() throws GeneralSecurityException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        try {
            keyStore.load(null);
        } catch (java.io.IOException e) {
            throw new GeneralSecurityException("Could not load Android Keystore", e);
        }

        java.security.Key existing = keyStore.getKey(KEY_ALIAS, null);
        if (existing instanceof SecretKey) return (SecretKey) existing;

        KeyGenerator generator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        generator.init(new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build());
        return generator.generateKey();
    }
}
