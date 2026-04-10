package org.kumaran.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.kumaran.entity.AppNotification;
import org.kumaran.entity.LeaveApplication;
import org.kumaran.entity.UserAccount;
import org.kumaran.repository.AppNotificationRepository;
import org.kumaran.repository.LeaveApplicationRepository;
import org.kumaran.repository.UserAccountRepository;
import org.kumaran.security.JwtRequestHelper;
import org.kumaran.service.LeaveTrackerService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.Base64;

@RestController
@RequestMapping("/api/leave-applications")
@Tag(name = "Leave Applications", description = "APIs for applying leave, manager/admin review workflow, and medical attachment access")
public class LeaveApplicationController {
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final UserAccountRepository userRepository;
    private final AppNotificationRepository appNotificationRepository;
    private final LeaveTrackerService leaveTrackerService;
    private final JwtRequestHelper jwtHelper;

    public LeaveApplicationController(LeaveApplicationRepository leaveApplicationRepository,
                                      UserAccountRepository userRepository,
                                      AppNotificationRepository appNotificationRepository,
                                      LeaveTrackerService leaveTrackerService,
                                      JwtRequestHelper jwtHelper) {
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.userRepository = userRepository;
        this.appNotificationRepository = appNotificationRepository;
        this.leaveTrackerService = leaveTrackerService;
        this.jwtHelper = jwtHelper;
    }

    @PostMapping
    @Operation(
        summary = "Apply Leave",
        description = "Creates a new leave request for employee/manager users. Sick leave for 3 or more days requires a medical attachment."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Leave request created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LeaveApplication.class))),
        @ApiResponse(responseCode = "400", description = "Invalid leave payload or reporting manager mapping issue",
            content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "403", description = "Role is not allowed to apply leave",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> applyLeave(@RequestBody LeaveApplication request, HttpServletRequest httpRequest) {
        Optional<UserAccount> actorOpt = jwtHelper.getActor(httpRequest);
        if (actorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        UserAccount actor = actorOpt.get();
        String role = safeLower(actor.getRole());
        if (!role.equals("employee") && !role.equals("manager")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only employee and manager accounts can apply leave");
        }

        if (request.getLeaveType() == null || request.getLeaveType().isBlank()
                || request.getFromDate() == null || request.getFromDate().isBlank()
                || request.getToDate() == null || request.getToDate().isBlank()
                || request.getDuration() == null || request.getDuration() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid leave payload");
        }

        String normalizedLeaveType = safeLower(request.getLeaveType());
        if (normalizedLeaveType.equals("sick") && request.getDuration() >= 3) {
            String attachmentData = Optional.ofNullable(request.getSickAttachmentData()).orElse("").trim();
            if (attachmentData.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Medical attachment is mandatory for sick leave of 3 or more days");
            }
        }

        LeaveApplication entity = new LeaveApplication();
        entity.setEmployeeId(actor.getEmployeeId());
        entity.setUsername(actor.getUsername());
        entity.setEmailId(actor.getEmailId());
        entity.setEmployeeName(buildDisplayName(actor));
        entity.setLeaveType(request.getLeaveType());
        entity.setFromDate(request.getFromDate());
        entity.setToDate(request.getToDate());
        entity.setDuration(request.getDuration());
        entity.setReason(request.getReason());
        entity.setSickAttachmentName(request.getSickAttachmentName());
        entity.setSickAttachmentData(request.getSickAttachmentData());
        entity.setStatus("PENDING");
        entity.setAppliedDate(LocalDate.now().toString());
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setUpdatedAt(System.currentTimeMillis());

        Optional<UserAccount> managerOpt = resolveManagerFor(actor, request);
        if (role.equals("employee") && managerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reporting manager is not mapped");
        }

        managerOpt.ifPresent(manager -> {
            entity.setReportingManagerId(manager.getEmployeeId());
            entity.setReportingManagerUsername(manager.getUsername());
            entity.setReportingManagerEmail(manager.getEmailId());
            entity.setReportingManagerName(buildDisplayName(manager));
        });

        LeaveApplication saved = leaveApplicationRepository.save(entity);

        managerOpt.ifPresent(manager -> createNotification(
                manager.getUsername(),
                "New Leave Request",
                entity.getEmployeeName() + " submitted a " + formatLeaveType(entity.getLeaveType())
                        + " request (" + entity.getDuration() + " day" + (entity.getDuration() > 1 ? "s" : "") + ").",
                "leave-request-submitted"
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/my")
    @Operation(
        summary = "Get My Leave Applications",
        description = "Returns leave requests for the currently authenticated user (matched by employeeId/username/emailId)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave requests retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LeaveApplication.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> getMyApplications(HttpServletRequest httpRequest) {
        Optional<UserAccount> actorOpt = jwtHelper.getActor(httpRequest);
        if (actorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        UserAccount actor = actorOpt.get();

        List<LeaveApplication> rows = leaveApplicationRepository
                .findByEmployeeIdOrUsernameOrEmailIdOrderByCreatedAtDesc(
                        actor.getEmployeeId(),
                        actor.getUsername(),
                        actor.getEmailId()
                );

        return ResponseEntity.ok(rows);
    }

    @GetMapping("/all")
    @Operation(
        summary = "Get All Leave Applications",
        description = "Admin-only endpoint that returns every leave request in the system."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "All leave requests retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LeaveApplication.class))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> getAllApplications(HttpServletRequest request) {
        if (!jwtHelper.isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        return ResponseEntity.ok(leaveApplicationRepository.findAll());
    }

    @GetMapping("/manager")
    @Operation(
        summary = "Get Manager Review Queue",
        description = "Returns leave requests that belong to a manager's subordinates and direct manager-routed requests."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Manager leave queue retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LeaveApplication.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> getManagerApplications(HttpServletRequest request) {
        Optional<UserAccount> actorOpt = jwtHelper.getActor(request);
        if (actorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        UserAccount manager = actorOpt.get();
        if (!safeLower(manager.getRole()).equals("manager")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        Set<String> subordinateKeys = userRepository.findAll().stream()
                .filter(user -> isSubordinateOf(user, manager))
                .flatMap(user -> Stream.of(user.getEmployeeId(), user.getUsername(), user.getEmailId()))
                .filter(Objects::nonNull)
                .map(this::normalizeKey)
            .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());

        Set<String> managerKeys = buildManagerKeys(manager);

        List<LeaveApplication> result = leaveApplicationRepository.findAll().stream()
                .filter(app -> belongsToSubordinate(app, subordinateKeys) || belongsToManager(app, managerKeys))
            .sorted(Comparator.comparing(
                LeaveApplication::getCreatedAt,
                Comparator.nullsLast(Long::compareTo)
            ).reversed())
                .toList();

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Update Leave Request Status",
        description = "Approves or rejects a pending leave request. Allowed for admins and authorized managers."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave status updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LeaveApplication.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status transition or invalid payload",
            content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "404", description = "Leave request not found",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> updateStatus(@Parameter(description = "Leave request ID", required = true, example = "101") @PathVariable Long id,
                                          @RequestBody Map<String, String> requestBody,
                                          HttpServletRequest request) {
        Optional<UserAccount> actorOpt = jwtHelper.getActor(request);
        if (actorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Long requestId = Objects.requireNonNull(id, "Leave request id is required");
        Optional<LeaveApplication> appOpt = leaveApplicationRepository.findById(requestId);
        if (appOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Leave request not found");
        }

        LeaveApplication app = appOpt.get();
        UserAccount actor = actorOpt.get();
        boolean admin = jwtHelper.isAdmin(request);
        boolean manager = safeLower(actor.getRole()).equals("manager");

        if (!admin && !manager) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        if (manager && !admin && !canManagerReview(actor, app)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        String status = Optional.ofNullable(requestBody.get("status")).orElse("").trim().toUpperCase(Locale.ROOT);
        if (!status.equals("APPROVED") && !status.equals("REJECTED")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Status must be APPROVED or REJECTED");
        }

        String comment = Optional.ofNullable(requestBody.get("comment")).orElse("").trim();
        String previousStatus = safeLower(app.getStatus());

        if (!previousStatus.equals("pending")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Only pending requests can be approved or rejected");
        }

        app.setStatus(status);
        app.setManagerComment(comment);
        app.setRejectionReason(status.equals("REJECTED") ? comment : "");
        app.setReviewedBy(actor.getUsername());
        app.setReviewedAt(Instant.now().toString());
        app.setUpdatedAt(System.currentTimeMillis());
        LeaveApplication saved = leaveApplicationRepository.save(app);

        if (status.equals("APPROVED")) {
            Optional<UserAccount> employeeOpt = findEmployeeForApplication(app);
            employeeOpt.ifPresent(employee ->
                leaveTrackerService.updateLeaveTrackerBookingOnApproval(employee)
            );
        }

        createNotification(
                app.getUsername(),
                status.equals("APPROVED") ? "Leave Request Approved" : "Leave Request Rejected",
                status.equals("APPROVED")
                        ? "Your leave request (" + formatLeaveType(app.getLeaveType()) + ") has been approved."
                        : "Your leave request (" + formatLeaveType(app.getLeaveType()) + ") has been rejected. " + (comment.isBlank() ? "" : "Reason: " + comment),
                status.equals("APPROVED") ? "leave-approved" : "leave-rejected"
        );

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}/attachment")
    @Operation(
        summary = "View or Download Medical Attachment",
        description = "Returns the medical attachment for a leave request. Set download=true for attachment disposition."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attachment returned successfully"),
        @ApiResponse(responseCode = "400", description = "Attachment content is invalid",
            content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "404", description = "Leave request or attachment not found",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> getSickAttachment(@Parameter(description = "Leave request ID", required = true, example = "101") @PathVariable Long id,
                                               @RequestParam(name = "download", defaultValue = "false") boolean download,
                                               HttpServletRequest request) {
        Optional<UserAccount> actorOpt = jwtHelper.getActor(request);
        if (actorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Optional<LeaveApplication> appOpt = leaveApplicationRepository.findById(id);
        if (appOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Leave request not found");
        }

        LeaveApplication app = appOpt.get();
        UserAccount actor = actorOpt.get();
        boolean admin = jwtHelper.isAdmin(request);
        boolean manager = safeLower(actor.getRole()).equals("manager");

        if (!admin) {
            if (manager && !canManagerReview(actor, app)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }
            if (!manager && !isRequestOwner(actor, app)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }
        }

        if (app.getSickAttachmentData() == null || app.getSickAttachmentData().isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No attachment available for this leave request");
        }

        AttachmentPayload payload = parseAttachmentData(app.getSickAttachmentData());
        if (payload == null || payload.bytes().length == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Attachment content is invalid");
        }

        String fileName = Optional.ofNullable(app.getSickAttachmentName())
                .filter(name -> !name.isBlank())
                .orElse("medical-attachment");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(payload.contentType()));
        headers.setContentLength(payload.bytes().length);
        headers.setCacheControl("no-store");
        headers.setContentDisposition(download
                ? ContentDisposition.attachment().filename(fileName).build()
                : ContentDisposition.inline().filename(fileName).build());

        return new ResponseEntity<>(payload.bytes(), headers, HttpStatus.OK);
    }

    private boolean canManagerReview(UserAccount manager, LeaveApplication app) {
        Set<String> managerKeys = buildManagerKeys(manager);
        return managerKeys.contains(normalizeKey(app.getReportingManagerId()))
                || managerKeys.contains(normalizeKey(app.getReportingManagerUsername()))
            || managerKeys.contains(normalizeKey(app.getReportingManagerEmail()))
            || managerKeys.contains(normalizeKey(app.getReportingManagerName()));
    }

    private boolean belongsToSubordinate(LeaveApplication app, Set<String> subordinateKeys) {
        return subordinateKeys.contains(normalizeKey(app.getEmployeeId()))
                || subordinateKeys.contains(normalizeKey(app.getUsername()))
                || subordinateKeys.contains(normalizeKey(app.getEmailId()));
    }

    private boolean belongsToManager(LeaveApplication app, Set<String> managerKeys) {
        return managerKeys.contains(normalizeKey(app.getReportingManagerId()))
                || managerKeys.contains(normalizeKey(app.getReportingManagerUsername()))
                || managerKeys.contains(normalizeKey(app.getReportingManagerEmail()))
                || managerKeys.contains(normalizeKey(app.getReportingManagerName()));
    }

    private Set<String> buildManagerKeys(UserAccount manager) {
        return Stream.of(
                        manager.getEmployeeId(),
                        manager.getUsername(),
                        manager.getEmailId(),
                        buildDisplayName(manager)
                )
                .map(this::normalizeKey)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());
    }

    private boolean isSubordinateOf(UserAccount user, UserAccount manager) {
        String reporting = normalizeKey(user.getReportingEmployeeId());
        return reporting.equals(normalizeKey(manager.getEmployeeId()))
                || reporting.equals(normalizeKey(manager.getUsername()));
    }

    private Optional<UserAccount> resolveManagerFor(UserAccount employee, LeaveApplication request) {
        String reportingId = normalizeBlank(employee.getReportingEmployeeId());

        if (reportingId != null) {
            Optional<UserAccount> byEmployeeId = userRepository.findByEmployeeId(reportingId);
            if (byEmployeeId.isPresent()) {
                return byEmployeeId;
            }
            Optional<UserAccount> byUsername = userRepository.findByUsername(reportingId);
            if (byUsername.isPresent()) {
                return byUsername;
            }
        }

        String managerUsername = normalizeBlank(request.getReportingManagerUsername());
        if (managerUsername != null) {
            return userRepository.findByUsername(managerUsername);
        }

        String managerEmail = normalizeBlank(request.getReportingManagerEmail());
        if (managerEmail != null) {
            return userRepository.findByEmailId(managerEmail);
        }

        String managerEmployeeId = normalizeBlank(request.getReportingManagerId());
        if (managerEmployeeId != null) {
            return userRepository.findByEmployeeId(managerEmployeeId);
        }

        return Optional.empty();
    }

    private Optional<UserAccount> findEmployeeForApplication(LeaveApplication app) {
        String employeeId = normalizeBlank(app.getEmployeeId());
        if (employeeId != null) {
            Optional<UserAccount> byEmployeeId = userRepository.findByEmployeeId(employeeId);
            if (byEmployeeId.isPresent()) {
                return byEmployeeId;
            }
        }

        String username = normalizeBlank(app.getUsername());
        if (username != null) {
            Optional<UserAccount> byUsername = userRepository.findByUsername(username);
            if (byUsername.isPresent()) {
                return byUsername;
            }
        }

        String email = normalizeBlank(app.getEmailId());
        if (email != null) {
            return userRepository.findByEmailId(email);
        }

        return Optional.empty();
    }

    private boolean isRequestOwner(UserAccount user, LeaveApplication app) {
        Set<String> actorKeys = Stream.of(user.getEmployeeId(), user.getUsername(), user.getEmailId())
                .map(this::normalizeKey)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());

        return actorKeys.contains(normalizeKey(app.getEmployeeId()))
                || actorKeys.contains(normalizeKey(app.getUsername()))
                || actorKeys.contains(normalizeKey(app.getEmailId()));
    }

    private AttachmentPayload parseAttachmentData(String rawData) {
        try {
            String raw = rawData == null ? "" : rawData.trim();
            if (raw.isBlank()) {
                return null;
            }

            String contentType = "application/octet-stream";
            String encoded = raw;

            if (raw.startsWith("data:")) {
                int commaIndex = raw.indexOf(',');
                if (commaIndex <= 0) {
                    return null;
                }

                String meta = raw.substring(5, commaIndex);
                encoded = raw.substring(commaIndex + 1);

                int separator = meta.indexOf(';');
                contentType = separator >= 0 ? meta.substring(0, separator) : meta;
                if (contentType.isBlank()) {
                    contentType = "application/octet-stream";
                }
            }

            byte[] bytes = Base64.getDecoder().decode(encoded);
            return new AttachmentPayload(contentType, bytes);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void createNotification(String recipientUsername, String title, String message, String type) {
        if (recipientUsername == null || recipientUsername.isBlank()) {
            return;
        }

        AppNotification notification = new AppNotification();
        notification.setRecipientUsername(recipientUsername);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(Instant.now().toString());
        appNotificationRepository.save(notification);
    }

    private String formatLeaveType(String leaveType) {
        String normalized = safeLower(leaveType);
        if (normalized.equals("sick")) {
            return "Sick Leave";
        }
        if (normalized.equals("casual")) {
            return "Casual Leave";
        }
        if (normalized.equals("lop")) {
            return "Leave Without Pay";
        }
        return leaveType;
    }

    private String buildDisplayName(UserAccount user) {
        String fullName = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        if (!fullName.isBlank()) {
            return fullName;
        }
        return user.getUsername();
    }

    private String normalizeKey(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record AttachmentPayload(String contentType, byte[] bytes) {
    }
}


