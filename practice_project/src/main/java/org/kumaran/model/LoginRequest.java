package org.kumaran.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login request payload")
public class LoginRequest {
    @Schema(description = "Username or email for login", example = "admin", required = true)
    private String username;

    @Schema(description = "User password", example = "password123", required = true)
    private String password;

    @Schema(description = "User role (admin or employee)", example = "admin", allowableValues = {"admin", "employee"}, required = true)
    private String role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
