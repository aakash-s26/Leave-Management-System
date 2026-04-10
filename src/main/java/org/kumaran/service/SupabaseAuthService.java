package org.kumaran.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kumaran.config.SupabaseProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class SupabaseAuthService {
    private final SupabaseProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public SupabaseAuthService(SupabaseProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public boolean isEnabled() {
        return properties.isAuthEnabled();
    }

    public boolean isConfiguredForLogin() {
        return hasText(properties.getUrl()) && hasText(properties.getAnonKey());
    }

    public boolean isConfiguredForAdmin() {
        return hasText(properties.getUrl()) && hasText(properties.getServiceRoleKey());
    }

    public boolean verifyCredentials(String email, String password) {
        if (!isEnabled()) {
            return true;
        }

        if (!isConfiguredForLogin()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Supabase login is enabled but not configured. Set SUPABASE_URL and SUPABASE_ANON_KEY");
        }

        try {
            String encodedGrant = URLEncoder.encode("password", StandardCharsets.UTF_8);
            URI uri = URI.create(trimTrailingSlash(properties.getUrl()) + "/auth/v1/token?grant_type=" + encodedGrant);

            Map<String, Object> payload = new HashMap<>();
            payload.put("email", email);
            payload.put("password", password);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofMillis(Math.max(1000, properties.getTimeoutMs())))
                    .header("Content-Type", "application/json")
                    .header("apikey", properties.getAnonKey())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Unable to verify credentials with Supabase auth", ex);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Unable to verify credentials with Supabase auth", ex);
        }
    }

    public void createSupabaseUser(String email,
                                   String password,
                                   String role,
                                   String employeeId,
                                   String firstName,
                                   String lastName) {
        if (!isEnabled()) {
            return;
        }

        if (!isConfiguredForAdmin()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Supabase auth is enabled but admin sync is not configured. Set SUPABASE_SERVICE_ROLE_KEY");
        }

        try {
            URI uri = URI.create(trimTrailingSlash(properties.getUrl()) + "/auth/v1/admin/users");

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("role", role);
            metadata.put("employeeId", employeeId);
            metadata.put("firstName", firstName);
            metadata.put("lastName", lastName);

            Map<String, Object> payload = new HashMap<>();
            payload.put("email", email);
            payload.put("password", password);
            payload.put("email_confirm", true);
            payload.put("user_metadata", metadata);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofMillis(Math.max(1000, properties.getTimeoutMs())))
                    .header("Content-Type", "application/json")
                    .header("apikey", properties.getServiceRoleKey())
                    .header("Authorization", "Bearer " + properties.getServiceRoleKey())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return;
            }

            String message = parseSupabaseError(response.body());
            if (response.statusCode() == 422 && message.toLowerCase().contains("already")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Supabase user already exists");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Supabase user provisioning failed: " + message);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Unable to provision user in Supabase auth", ex);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Unable to provision user in Supabase auth", ex);
        }
    }

    private String parseSupabaseError(String body) {
        if (!hasText(body)) {
            return "Unknown Supabase error";
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.hasNonNull("msg")) {
                return node.get("msg").asText();
            }
            if (node.hasNonNull("message")) {
                return node.get("message").asText();
            }
        } catch (Exception ignored) {
            // Fallback to raw body
        }
        return body;
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

