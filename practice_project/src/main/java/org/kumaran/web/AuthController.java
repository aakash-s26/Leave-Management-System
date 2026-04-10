package org.kumaran.web;

import jakarta.servlet.http.HttpServletRequest;
import org.kumaran.model.CreateUserRequest;
import org.kumaran.model.LoginRequest;
import org.kumaran.model.UserAccount;
import org.kumaran.model.UserResponse;
import org.kumaran.repository.UserAccountRepository;
import org.kumaran.security.JwtUtil;
import org.kumaran.service.LeaveTrackerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Tag(name = "Authentication & User Management", description = "APIs for user authentication, profile management, and employee administration")
public class AuthController {
    private final UserAccountRepository userRepository;
    private final LeaveTrackerService leaveTrackerService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserAccountRepository userRepository, LeaveTrackerService leaveTrackerService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.leaveTrackerService = leaveTrackerService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/auth/login")
    @Operation(
        summary = "User Login",
        description = "Authenticate user with username, password and role. Returns user profile on successful authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid username, password, or role",
            content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<UserAccount> account = userRepository.findByUsername(request.getUsername());
        if (account.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        UserAccount user = account.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        if (request.getRole() != null && !request.getRole().isBlank() &&
                !user.getRole().equalsIgnoreCase(request.getRole())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        UserResponse response = UserResponse.from(user);
        response.setToken(token);
        response.setRedirectUrl(user.getRole().equalsIgnoreCase("admin") ? "/admin-dashboard.html" : "/dashboard.html");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{username}")
    @Operation(
        summary = "Get User Profile",
        description = "Retrieve user profile information by username"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> getUserProfile(
        @Parameter(description = "Username of the user", required = true, example = "admin")
        @PathVariable String username,
        HttpServletRequest request) {
        if (!isSelfOrAdmin(username, request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        Optional<UserAccount> account = userRepository.findByUsername(username);
        if (account.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.ok(UserResponse.from(account.get()));
    }

    @PutMapping("/users/{username}")
    @Operation(
        summary = "Update User Profile",
        description = "Update user profile information. Only mutable fields (phone, nationality, etc.) can be updated for employees. Immutable fields are preserved."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> updateUserProfile(
        @Parameter(description = "Username of the user to update", required = true, example = "employee@company.com")
        @PathVariable String username,
        @RequestBody UserResponse request,
        HttpServletRequest httpRequest) {
        if (!isSelfOrAdmin(username, httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        Optional<UserAccount> account = userRepository.findByUsername(username);
        if (account.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        UserAccount user = account.get();
        // Preserve immutable profile values for employees
        user.setPhoneNumber(request.getPhoneNumber());
        user.setNationality(request.getNationality());
        user.setBloodGroup(request.getBloodGroup());
        user.setMaritalStatus(request.getMaritalStatus());
        user.setDob(request.getDob());
        user.setPersonalEmail(request.getPersonalEmail());
        user.setGender(request.getGender());
        user.setAddress(request.getAddress());

        userRepository.save(user);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/users")
    @Operation(
        summary = "Create New User",
        description = "Create a new user account. For employees, auto-generates employee ID if not provided. Sets joining date to current date."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "409", description = "Username or Employee ID already exists",
            content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request,
                                        HttpServletRequest requestContext) {
        if (!isAdmin(requestContext)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin users can create accounts");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()) {
            if (userRepository.findByEmployeeId(request.getEmployeeId()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Employee ID already exists");
            }
        }

        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setEmployeeId(request.getRole() != null && request.getRole().equalsIgnoreCase("employee") ?
                (request.getEmployeeId() != null && !request.getEmployeeId().isBlank() ? request.getEmployeeId() : generateNextEmployeeId()) : null);
        user.setEmailId(request.getEmailId());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDepartment(request.getDepartment());
        user.setDesignation(request.getDesignation());
        user.setReportingEmployeeId(request.getReportingEmployeeId());
        user.setLocation(request.getLocation());
        user.setJoining(request.getJoining());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setNationality(request.getNationality());
        user.setBloodGroup(request.getBloodGroup());
        user.setMaritalStatus(request.getMaritalStatus());
        user.setDob(request.getDob());
        user.setPersonalEmail(request.getPersonalEmail());
        user.setGender(request.getGender());
        user.setAddress(request.getAddress());

        userRepository.save(user);

        // Auto-sync leave tracker for employee
        if (user.getRole() != null && user.getRole().equalsIgnoreCase("employee")) {
            leaveTrackerService.syncLeaveTrackerForEmployee(user, 0, 0, 0);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    private String generateNextEmployeeId() {
        List<UserAccount> allUsers = userRepository.findAll();
        int maxSequence = allUsers.stream()
                .map(UserAccount::getEmployeeId)
                .filter(Objects::nonNull)
                .map(id -> {
                    Matcher matcher = Pattern.compile("LP-(\\d+)").matcher(id);
                    if (matcher.matches()) {
                        return Integer.parseInt(matcher.group(1));
                    }
                    return 0;
                })
                .max(Integer::compareTo)
                .orElse(0);
        return String.format("LP-%03d", maxSequence + 1);
    }

    @GetMapping("/users")
    @Operation(
        summary = "Get All Users",
        description = "Retrieve a list of all users in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<List<UserResponse>> getAllUsers(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<UserResponse> users = userRepository.findAll().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }

    private boolean isAdmin(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) {
            return false;
        }
        String role = jwtUtil.getRoleFromToken(token);
        return role != null && role.equalsIgnoreCase("admin");
    }

    private boolean isSelfOrAdmin(String username, HttpServletRequest request) {
        if (isAdmin(request)) {
            return true;
        }
        String token = getTokenFromRequest(request);
        if (token == null) {
            return false;
        }
        String authUsername = jwtUtil.getUsernameFromToken(token);
        return authUsername != null && authUsername.equalsIgnoreCase(username);
    }

    private boolean equalsIgnoreCase(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }
}
