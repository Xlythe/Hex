package com.xlythe.hex.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.xlythe.hex.server.IgGameCenterModels.UserSession;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class ServerCredentialCodecTest {
    @Test
    public void roundTripsPasswordEquivalentDigestAndSession() throws Exception {
        ServerCredentials original = new ServerCredentials(
                "Alice",
                "2ab96390c7dbe3439de74d0c9b0b1767",
                new UserSession("7", "Alice Display", "private-session-token"));

        byte[] encoded = ServerCredentialCodec.encode(original);
        ServerCredentials restored = ServerCredentialCodec.decode(encoded);

        assertEquals(original.username, restored.username);
        assertEquals(original.passwordDigest, restored.passwordDigest);
        assertEquals(original.session.uid, restored.session.uid);
        assertEquals(original.session.name, restored.session.name);
        assertEquals(original.session.sessionId, restored.session.sessionId);
    }

    @Test(expected = IOException.class)
    public void rejectsTruncatedPayload() throws Exception {
        ServerCredentials credentials = new ServerCredentials(
                "Alice",
                "2ab96390c7dbe3439de74d0c9b0b1767",
                new UserSession("7", "Alice", "token"));
        byte[] encoded = ServerCredentialCodec.encode(credentials);
        ServerCredentialCodec.decode(Arrays.copyOf(encoded, encoded.length - 2));
    }

    @Test
    public void rejectsPlaintextInsteadOfDigest() {
        try {
            new ServerCredentials(
                    "Alice", "hunter2", new UserSession("7", "Alice", "token"));
        } catch (IllegalArgumentException expected) {
            assertFalse(expected.getMessage().isEmpty());
            return;
        }
        throw new AssertionError("Expected plaintext password rejection");
    }
}
