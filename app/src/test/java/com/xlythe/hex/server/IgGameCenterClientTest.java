package com.xlythe.hex.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import com.xlythe.hex.server.IgGameCenterModels.ApiException;
import com.xlythe.hex.server.IgGameCenterModels.BoardRef;
import com.xlythe.hex.server.IgGameCenterModels.HandlerResponse;
import com.xlythe.hex.server.IgGameCenterModels.UserSession;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class IgGameCenterClientTest {
    private static final String LOGIN_XML =
            "<loginResult><uid>7</uid><name>Alice</name>"
                    + "<session_id>token</session_id></loginResult>";
    private static final String REGISTER_XML =
            "<registrationResult><uid>7</uid><name>Alice</name></registrationResult>";
    private static final String HANDLER_XML =
            "<handlerData><sessionInfo status=\"INIT\" owner=\"7\"/>"
                    + "<memberInfo active=\"0\" finished=\"0\" place=\"1\"/>"
                    + "</handlerData>";

    @Test
    public void registrationSendsLegacyPlaintextOnlyForRegistration() throws Exception {
        RecordingTransport transport = new RecordingTransport(REGISTER_XML);
        IgGameCenterClient client = new IgGameCenterClient(transport, "device-1");

        client.register("Alice & Bob", "plain secret", "alice@example.com");

        assertEquals("/api_user_add.php", transport.path);
        assertEquals("Alice & Bob", transport.parameters.get("name"));
        assertEquals("plain secret", transport.parameters.get("password"));
        assertFalse(transport.parameters.containsKey("md5"));
        assertEquals("device-1", transport.parameters.get("networkuid"));
    }

    @Test
    public void loginHashesPasswordAndMarksItAsMd5() throws Exception {
        RecordingTransport transport = new RecordingTransport(LOGIN_XML);
        IgGameCenterClient client = new IgGameCenterClient(transport, "device-1");

        client.login("Alice", "hunter2");

        assertEquals("/api_login.php", transport.path);
        assertEquals("2ab96390c7dbe3439de74d0c9b0b1767",
                transport.parameters.get("password"));
        assertEquals("1", transport.parameters.get("md5"));
    }

    @Test
    public void syncReadsConfiguredClientAndSendsCodeOnlyWithUserSession() throws Exception {
        String id = "12345-client.apps.googleusercontent.com";
        RecordingTransport config = new RecordingTransport("<response><client_id>" + id + "</client_id></response>");
        assertEquals(id, new IgGameCenterClient(config, "device").playGamesClientId());
        assertFalse(config.parameters.containsKey("session_id"));
        RecordingTransport sync = new RecordingTransport("<synced/>");
        new IgGameCenterClient(sync, "device").syncPlayGames(new UserSession("7", "Alice", "session"), "one-time");
        assertEquals("/api_play_sync.php", sync.path);
        assertEquals("session", sync.parameters.get("session_id"));
        assertEquals("one-time", sync.parameters.get("code"));
    }

    @Test
    public void unconfiguredServerAndUnconfirmedSyncAreReportedAsFailures() {
        IgGameCenterClient client = new IgGameCenterClient(new RecordingTransport("<response/>"), "device");
        assertThrows(ApiException.class, client::playGamesClientId);
        assertThrows(ApiException.class, () -> client.syncPlayGames(new UserSession("7", "Alice", "session"), "one-time"));
    }

    @Test
    public void initialHandshakeOmitsCommandAndRoutesThroughValidatedServer() throws Exception {
        RecordingTransport transport = new RecordingTransport(HANDLER_XML);
        IgGameCenterClient client = new IgGameCenterClient(transport, "device-1");
        UserSession user = new UserSession("7", "Alice", "token");

        HandlerResponse response = client.handle(
                new BoardRef("42", "gc1"), user, 9, null, null);

        assertEquals("/server/gc1/api_handler.php", transport.path);
        assertNull(transport.parameters.get("cmd"));
        assertEquals("9", transport.parameters.get("lasteid"));
        assertEquals("INIT", response.status);
    }

    @Test
    public void formEncodingUsesUtf8AndNeverPlacesSecretsInUrl() {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("name", "Alice & Bob");
        parameters.put("password", "こんにちは + ?");

        assertEquals(
                "name=Alice+%26+Bob&password=%E3%81%93%E3%82%93%E3%81%AB"
                        + "%E3%81%A1%E3%81%AF+%2B+%3F",
                IgGameCenterTransport.Http.encodeForm(parameters));
    }

    private static final class RecordingTransport implements IgGameCenterTransport {
        private final String response;
        String path;
        Map<String, String> parameters;

        RecordingTransport(String response) {
            this.response = response;
        }

        @Override
        public String post(String path, Map<String, String> parameters) {
            this.path = path;
            this.parameters = new LinkedHashMap<>(parameters);
            return response;
        }
    }
}
