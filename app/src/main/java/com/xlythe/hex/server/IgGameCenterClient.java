package com.xlythe.hex.server;

import static com.xlythe.hex.server.IgGameCenterModels.ApiException;

import com.xlythe.hex.server.IgGameCenterModels.BoardRef;
import com.xlythe.hex.server.IgGameCenterModels.HandlerResponse;
import com.xlythe.hex.server.IgGameCenterModels.LobbyBoard;
import com.xlythe.hex.server.IgGameCenterModels.RegisteredUser;
import com.xlythe.hex.server.IgGameCenterModels.UserSession;
import com.xlythe.hex.BuildConfig;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Typed client for the POST/XML igGameCenter API used by the original Android app. */
public final class IgGameCenterClient {
    private final IgGameCenterTransport transport;
    private final String networkUid;

    public IgGameCenterClient(IgGameCenterTransport transport, String networkUid) {
        if (transport == null) throw new NullPointerException("transport");
        if (networkUid == null || networkUid.trim().isEmpty()) {
            throw new IllegalArgumentException("A stable network UID is required");
        }
        this.transport = transport;
        this.networkUid = networkUid;
    }

    public static IgGameCenterClient production(String networkUid) {
        return new IgGameCenterClient(
                new IgGameCenterTransport.Http(
                        BuildConfig.HEX_API_BASE_URL,
                        (int) IgGameCenterProtocol.POLL_INTERVAL_MS),
                networkUid);
    }

    /**
     * The registration endpoint requires the original plaintext password.
     * It is used only for this request and is never retained by this client.
     */
    public RegisteredUser register(String username, String plaintextPassword, String email)
            throws ApiException {
        LinkedHashMap<String, String> parameters = common();
        parameters.put("name", username);
        parameters.put("password", plaintextPassword);
        parameters.put("networkuid", networkUid);
        if (email != null && !email.trim().isEmpty()) parameters.put("email", email.trim());
        return IgGameCenterXml.parseRegistration(
                transport.post("/api_user_add.php", parameters));
    }

    public UserSession login(String username, String plaintextPassword) throws ApiException {
        return loginWithDigest(username, IgGameCenterProtocol.md5(plaintextPassword));
    }

    public UserSession loginWithDigest(String username, String passwordDigest)
            throws ApiException {
        LinkedHashMap<String, String> parameters = common();
        parameters.put("login", username);
        parameters.put("password", passwordDigest);
        parameters.put("networkuid", networkUid);
        parameters.put("md5", "1");
        return IgGameCenterXml.parseLogin(transport.post("/api_login.php", parameters));
    }

    public BoardRef createBoard(UserSession user, String place) throws ApiException {
        LinkedHashMap<String, String> parameters = authenticated(user);
        parameters.put("gid", IgGameCenterProtocol.HEX_GAME_ID);
        if (place != null) parameters.put("place", place);
        return IgGameCenterXml.parseBoardRef(
                transport.post("/api_board_create.php", parameters));
    }

    public BoardRef joinRandomBoard(UserSession user, String place) throws ApiException {
        LinkedHashMap<String, String> parameters = authenticated(user);
        parameters.put("gid", IgGameCenterProtocol.HEX_GAME_ID);
        if (place != null) parameters.put("place", place);
        return IgGameCenterXml.parseBoardRef(
                transport.post("/api_board_random.php", parameters));
    }

    public List<LobbyBoard> fetchLobby(UserSession user) throws ApiException {
        LinkedHashMap<String, String> parameters = authenticated(user);
        parameters.put("gid", IgGameCenterProtocol.HEX_GAME_ID);
        return IgGameCenterXml.parseLobby(
                transport.post("/api_board_list.php", parameters));
    }

    /** A null command performs the required initial board handshake. */
    public HandlerResponse handle(
            BoardRef board,
            UserSession user,
            long lastEventId,
            String command,
            Map<String, String> commandParameters) throws ApiException {
        if (board == null || !IgGameCenterProtocol.isValidServerName(board.server)) {
            throw new IllegalArgumentException("Invalid game server");
        }
        LinkedHashMap<String, String> parameters = authenticated(user);
        parameters.put("sid", board.sid);
        parameters.put("lasteid", Long.toString(Math.max(0, lastEventId)));
        if (command != null && !command.trim().isEmpty()) {
            parameters.put("cmd", command.toUpperCase(Locale.ROOT));
        }
        if (commandParameters != null) parameters.putAll(commandParameters);
        return IgGameCenterXml.parseHandler(transport.post(
                "/server/" + board.server + "/api_handler.php",
                parameters));
    }

    public HandlerResponse refresh(BoardRef board, UserSession user, long lastEventId)
            throws ApiException {
        return handle(board, user, lastEventId, "REFRESH", null);
    }

    public HandlerResponse command(
            BoardRef board,
            UserSession user,
            long lastEventId,
            String command) throws ApiException {
        return handle(board, user, lastEventId, command, null);
    }

    public HandlerResponse command(
            BoardRef board,
            UserSession user,
            long lastEventId,
            String command,
            String key,
            String value) throws ApiException {
        LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
        parameters.put(key, value);
        return handle(board, user, lastEventId, command, parameters);
    }

    private LinkedHashMap<String, String> common() {
        LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
        parameters.put("app_id", IgGameCenterProtocol.APP_ID);
        parameters.put("app_code", IgGameCenterProtocol.APP_CODE);
        return parameters;
    }

    private LinkedHashMap<String, String> authenticated(UserSession user) {
        if (user == null
                || user.uid == null
                || user.sessionId == null
                || user.uid.isEmpty()
                || user.sessionId.isEmpty()) {
            throw new IllegalArgumentException("An authenticated user is required");
        }
        LinkedHashMap<String, String> parameters = common();
        parameters.put("uid", user.uid);
        parameters.put("session_id", user.sessionId);
        return parameters;
    }
}
