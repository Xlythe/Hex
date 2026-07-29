package com.xlythe.hex.server;

import static com.xlythe.hex.server.IgGameCenterModels.ApiException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Injectable POST transport so the legacy service can be tested without network access. */
public interface IgGameCenterTransport {
    String post(String path, Map<String, String> parameters) throws ApiException;

    /** HTTPS production transport. Request bodies and responses are deliberately never logged. */
    final class Http implements IgGameCenterTransport {
        private static final int MAX_RESPONSE_BYTES = 1024 * 1024;

        private final String baseUrl;
        private final int timeoutMs;

        public Http(String baseUrl, int timeoutMs) {
            String normalized = baseUrl == null ? "" : baseUrl.replaceAll("/+$", "");
            URI uri;
            try {
                uri = URI.create(normalized);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid igGameCenter API base URL", e);
            }
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null
                    || uri.getRawQuery() != null
                    || uri.getRawFragment() != null) {
                throw new IllegalArgumentException("igGameCenter API base URL must be HTTPS");
            }
            if (timeoutMs <= 0) throw new IllegalArgumentException("Timeout must be positive");
            this.baseUrl = normalized;
            this.timeoutMs = timeoutMs;
        }

        @Override
        public String post(String path, Map<String, String> parameters) throws ApiException {
            if (path == null || !path.startsWith("/") || path.contains("://")) {
                throw new IllegalArgumentException("API path must be relative");
            }

            HttpURLConnection connection = null;
            try {
                byte[] body = encodeForm(parameters).getBytes(StandardCharsets.UTF_8);
                connection = (HttpURLConnection) URI.create(baseUrl + path).toURL().openConnection();
                connection.setConnectTimeout(timeoutMs);
                connection.setReadTimeout(timeoutMs);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Accept", "application/xml, text/xml");
                connection.setRequestProperty(
                        "Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                connection.setFixedLengthStreamingMode(body.length);
                connection.setDoOutput(true);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(body);
                }

                int status = connection.getResponseCode();
                if (status < 200 || status >= 300) {
                    throw new ApiException("igGameCenter returned HTTP " + status);
                }
                try (InputStream input = connection.getInputStream()) {
                    return readBounded(input);
                }
            } catch (ApiException e) {
                throw e;
            } catch (IOException | IllegalArgumentException e) {
                throw new ApiException("Could not connect to igGameCenter", e);
            } finally {
                if (connection != null) connection.disconnect();
            }
        }

        static String encodeForm(Map<String, String> parameters) {
            StringBuilder encoded = new StringBuilder();
            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                if (parameter.getValue() == null) continue;
                if (encoded.length() > 0) encoded.append('&');
                encoded.append(urlEncode(parameter.getKey()));
                encoded.append('=');
                encoded.append(urlEncode(parameter.getValue()));
            }
            return encoded.toString();
        }

        private static String urlEncode(String value) {
            try {
                return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
            } catch (IOException impossible) {
                throw new AssertionError("Android must support UTF-8", impossible);
            }
        }

        private static String readBounded(InputStream input) throws IOException, ApiException {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                if (output.size() + count > MAX_RESPONSE_BYTES) {
                    throw new ApiException("igGameCenter response was unexpectedly large");
                }
                output.write(buffer, 0, count);
            }
            return output.toString(StandardCharsets.UTF_8.name());
        }
    }
}
