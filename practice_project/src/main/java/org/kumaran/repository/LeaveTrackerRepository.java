package org.kumaran.repository;

import org.kumaran.model.LeaveTrackerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface LeaveTrackerRepository extends JpaRepository<LeaveTrackerData, Long> {
    Optional<LeaveTrackerData> findByEmployeeId(String employeeId);
    List<LeaveTrackerData> findByDepartment(String department);
    List<LeaveTrackerData> findAll();
}
