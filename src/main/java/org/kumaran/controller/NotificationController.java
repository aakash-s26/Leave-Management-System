package org.kumaran.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.kumaran.entity.AppNotification;
import org.kumaran.repository.AppNotificationRepository;
import org.kumaran.security.JwtRequestHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.kumaran.entity.UserAccount;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "APIs for user notification inbox and read-state updates")
public class NotificationController {
    private final AppNotificationRepository appNotificationRepository;
    private final JwtRequestHelper jwtHelper;

    public NotificationController(AppNotificationRepository appNotificationRepository,
                                  JwtRequestHelper jwtHelper) {
        this.appNotificationRepository = appNotificationRepository;
        this.jwtHelper = jwtHelper;
    }

    @GetMapping("/my")
    @Operation(
        summary = "Get My Notifications",
        description = "Returns notifications for the authenticated user ordered by newest first."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppNotification.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> myNotifications(HttpServletRequest request) {
        Optional<UserAccount> actorOpt = jwtHelper.getActor(request);
        if (actorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String username = actorOpt.get().getUsername();
        List<AppNotification> rows = appNotificationRepository.findByRecipientUsernameOrderByCreatedAtDesc(username);
        return ResponseEntity.ok(rows);
    }

    @PostMapping("/mark-all-read")
    @Operation(
        summary = "Mark All Notifications As Read",
        description = "Marks all notifications for the authenticated user as read and returns number of updated rows."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notifications updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> markAllRead(HttpServletRequest request) {
        Optional<UserAccount> actorOpt = jwtHelper.getActor(request);
        if (actorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String username = actorOpt.get().getUsername();
        List<AppNotification> rows = appNotificationRepository.findByRecipientUsernameOrderByCreatedAtDesc(username);
        String now = Instant.now().toString();
        rows.forEach(row -> {
            row.setRead(true);
            row.setReadAt(now);
        });
        appNotificationRepository.saveAll(rows);
        return ResponseEntity.ok(Map.of("updated", rows.size()));
    }
}


