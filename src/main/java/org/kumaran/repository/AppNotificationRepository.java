package org.kumaran.repository;

import org.kumaran.entity.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findByRecipientUsernameOrderByCreatedAtDesc(String recipientUsername);
}

