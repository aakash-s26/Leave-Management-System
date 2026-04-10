package org.kumaran.repository;

import org.kumaran.entity.LeaveApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {
    List<LeaveApplication> findByEmployeeIdOrUsernameOrEmailIdOrderByCreatedAtDesc(String employeeId, String username, String emailId);
}

