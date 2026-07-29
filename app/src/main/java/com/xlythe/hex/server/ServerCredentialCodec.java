package com.xlythe.hex.server;

import com.xlythe.hex.server.IgGameCenterModels.UserSession;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Versioned binary payload encrypted by {@link ServerCredentialStore}. */
final class ServerCredentialCodec {
    private static final int VERSION = 1;

    private ServerCredentialCodec() {}

    static byte[] encode(ServerCredentials credentials) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                output.writeInt(VERSION);
                output.writeUTF(credentials.username);
                output.writeUTF(credentials.passwordDigest);
                output.writeUTF(credentials.session.uid);
                output.writeUTF(credentials.session.name);
                output.writeUTF(credentials.session.sessionId);
            }
            return bytes.toByteArray();
        } catch (IOException impossible) {
            throw new AssertionError("In-memory credential encoding failed", impossible);
        }
    }

    static ServerCredentials decode(byte[] payload) throws IOException {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(payload))) {
            int version = input.readInt();
            if (version != VERSION) throw new IOException("Unsupported credential version");
            ServerCredentials credentials = new ServerCredentials(
                    input.readUTF(),
                    input.readUTF(),
                    new UserSession(input.readUTF(), input.readUTF(), input.readUTF()));
            if (input.read() != -1) throw new IOException("Trailing credential data");
            return credentials;
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IOException("Invalid credential payload", e);
        }
    }
}
