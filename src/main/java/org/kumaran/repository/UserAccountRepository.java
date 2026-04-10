package org.kumaran.repository;

import org.kumaran.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
    Optional<UserAccount> findByEmployeeId(String employeeId);
    Optional<UserAccount> findByEmailId(String emailId);
    Optional<UserAccount> findByPersonalEmail(String personalEmail);
}

