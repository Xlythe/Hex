package com.xlythe.hex.legacy;

import android.os.Build;

import com.xlythe.hex.BuildConfig;
import com.xlythe.hex.server.IgGameCenterClient;
import com.xlythe.hex.server.IgGameCenterProtocol;
import com.xlythe.hex.server.IgGameCenterTransport;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;

/** Requests the TLS 1.2 context on systems where the default client still selects TLS 1.0. */
final class LegacyServer {
    private LegacyServer() {}

    static IgGameCenterClient connect(String deviceId) {
        if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 20) {
            try {
                SSLContext tls = SSLContext.getInstance("TLSv1.2");
                tls.init(null, null, null);
                return new IgGameCenterClient(new IgGameCenterTransport.Http(BuildConfig.HEX_API_BASE_URL,
                        (int) IgGameCenterProtocol.POLL_INTERVAL_MS, tls.getSocketFactory()), deviceId);
            } catch (GeneralSecurityException unavailable) {
                // Some devices customize their TLS provider. Use its normal verified HTTPS client.
            }
        }
        return IgGameCenterClient.production(deviceId);
    }
}
