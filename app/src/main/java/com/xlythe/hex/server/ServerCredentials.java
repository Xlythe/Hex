package com.xlythe.hex.server;

import com.xlythe.hex.server.IgGameCenterModels.UserSession;

import java.util.Locale;

/** Password-equivalent login digest and its last valid server session. */
public final class ServerCredentials {
    public final String username;
    public final String passwordDigest;
    public final UserSession session;

    public ServerCredentials(String username, String passwordDigest, UserSession session) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (passwordDigest == null
                || !passwordDigest.matches("(?i)^[0-9a-f]{32}$")) {
            throw new IllegalArgumentException("Expected an MD5 password digest");
        }
        if (session == null) throw new NullPointerException("session");
        this.username = username;
        this.passwordDigest = passwordDigest.toLowerCase(Locale.ROOT);
        this.session = session;
    }
}
