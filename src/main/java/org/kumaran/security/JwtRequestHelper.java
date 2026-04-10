package org.kumaran.security;

import jakarta.servlet.http.HttpServletRequest;
import org.kumaran.model.UserAccount;
import org.kumaran.repository.UserAccountRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Centralises all JWT-from-request operations so controllers share a single
 * implementation instead of each carrying identical private helper methods.
 */
@Component
public class JwtRequestHelper {

    private final JwtUtil jwtUtil;
    private final UserAccountRepository userRepository;

    public JwtRequestHelper(JwtUtil jwtUtil, UserAccountRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    /** Extracts the raw JWT string from the Authorization header, or null. */
    public String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }

    /** Returns the username embedded in the Bearer token, or null. */
    public String extractUsername(HttpServletRequest request) {
        String token = extractToken(request);
        return token != null ? jwtUtil.getUsernameFromToken(token) : null;
    }

    /** Returns the role embedded in the Bearer token, or null. */
    public String extractRole(HttpServletRequest request) {
        String token = extractToken(request);
        return token != null ? jwtUtil.getRoleFromToken(token) : null;
    }

    /** True when the Bearer token belongs to a user with role "admin". */
    public boolean isAdmin(HttpServletRequest request) {
        String role = extractRole(request);
        return role != null && role.equalsIgnoreCase("admin");
    }

    /** True when the request belongs to {@code username} themselves OR an admin. */
    public boolean isSelfOrAdmin(String username, HttpServletRequest request) {
        if (isAdmin(request)) return true;
        String authUsername = extractUsername(request);
        return authUsername != null && authUsername.equalsIgnoreCase(username);
    }

    /**
     * Resolves the UserAccount for the authenticated caller.
     * Returns empty if the token is missing, invalid, or the user no longer exists.
     */
    public Optional<UserAccount> getActor(HttpServletRequest request) {
        String username = extractUsername(request);
        if (username == null) return Optional.empty();
        return userRepository.findByUsername(username);
    }

    /** Delegates token generation to JwtUtil. */
    public String generateToken(String username, String role) {
        return jwtUtil.generateToken(username, role);
    }
}
