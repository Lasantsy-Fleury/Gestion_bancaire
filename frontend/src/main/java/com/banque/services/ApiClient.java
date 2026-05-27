package com.banque.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:5000";
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static String authToken;

    public static String get(String endpoint) throws Exception {
        String url = BASE_URL + endpoint;
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url.replace(" ", "%20")))
            .GET();
        addAuthHeader(builder);
        HttpRequest request = builder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException(extractErrorMessage(response.body()));
        }
        return response.body();
    }

    public static String post(String endpoint, Object body) throws Exception {
        String jsonBody = mapper.writeValueAsString(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + endpoint))
            .header("Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));
        addAuthHeader(builder);
        HttpRequest request = builder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException(extractErrorMessage(response.body()));
        }
        return response.body();
    }

    public static String login(String username, String password) throws Exception {
        String payload = mapper.writeValueAsString(new LoginRequest(username, password));
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/auth/login"))
            .header("Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException(extractErrorMessage(response.body()));
        }
        String body = response.body();
        String token = mapper.readTree(body).get("token").asText();
        setAuthToken(token);
        return token;
    }
    
    public static ObjectMapper getMapper() {
        return mapper;
    }

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static String getAuthToken() {
        return authToken;
    }

    private static void addAuthHeader(HttpRequest.Builder builder) {
        if (authToken != null && !authToken.isEmpty()) {
            builder.header("Authorization", "Bearer " + authToken);
        }
    }

    private static String extractErrorMessage(String body) {
        if (body == null || body.isEmpty()) {
            return "Une erreur est survenue. Veuillez reessayer.";
        }
        try {
            JsonNode node = mapper.readTree(body);
            if (node.has("error")) {
                return node.get("error").asText();
            }
            if (node.has("message")) {
                return node.get("message").asText();
            }
        } catch (Exception ignored) {
            // fall through
        }
        return body;
    }

    private static final class LoginRequest {
        public final String username;
        public final String password;

        private LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
