package org.kumaran.web;

import jakarta.servlet.http.HttpServletRequest;
import org.kumaran.model.AppNotification;
import org.kumaran.repository.AppNotificationRepository;
import org.kumaran.security.JwtRequestHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.kumaran.model.UserAccount;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final AppNotificationRepository appNotificationRepository;
    private final JwtRequestHelper jwtHelper;

    public NotificationController(AppNotificationRepository appNotificationRepository,
                                  JwtRequestHelper jwtHelper) {
        this.appNotificationRepository = appNotificationRepository;
        this.jwtHelper = jwtHelper;
    }

    @GetMapping("/my")
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
