package org.kumaran.service;

import org.kumaran.model.LeaveTrackerData;
import org.kumaran.model.UserAccount;
import org.kumaran.repository.LeaveTrackerRepository;
import org.kumaran.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveTrackerService {
    private final LeaveTrackerRepository leaveTrackerRepository;
    private final UserAccountRepository userRepository;

    public LeaveTrackerService(LeaveTrackerRepository leaveTrackerRepository, UserAccountRepository userRepository) {
        this.leaveTrackerRepository = leaveTrackerRepository;
        this.userRepository = userRepository;
    }

    /**
     * Calculate leave accrual based on joining date and current cycle (April - March)
     */
    public int calculateCycleAccrual(String joiningDateString) {
        if (joiningDateString == null || joiningDateString.isEmpty()) {
            return 0;
        }

        try {
            LocalDate joinDate = parseFlexibleDate(joiningDateString);
            if (joinDate == null) {
                return 0;
            }

            int joinMonth = joinDate.getMonthValue();
            int joinYear = joinDate.getYear();
            LocalDate cycleEnd;

            if (joinMonth >= 4) {
                cycleEnd = LocalDate.of(joinYear + 1, 3, 31);
            } else {
                cycleEnd = LocalDate.of(joinYear, 3, 31);
            }

            LocalDate effectiveStart = joinDate;
            if (effectiveStart.isAfter(cycleEnd)) {
                return 0;
            }

            return monthsInclusive(effectiveStart, cycleEnd);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Parse date in flexible formats
     */
    private LocalDate parseFlexibleDate(String dateString) {
        try {
            return LocalDate.parse(dateString);
        } catch (Exception e1) {
            try {
                String[] parts = dateString.split("-");
                if (parts.length == 3) {
                    int year = Integer.parseInt(parts[2]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[0]);
                    return LocalDate.of(year, month, day);
                }
            } catch (Exception e2) {
                // Ignore
            }
            return null;
        }
    }

    /**
     * Count months inclusively between two dates
     */
    private int monthsInclusive(LocalDate start, LocalDate end) {
        YearMonth startMonth = YearMonth.from(start);
        YearMonth endMonth = YearMonth.from(end);
        return (int) startMonth.until(endMonth, java.time.temporal.ChronoUnit.MONTHS) + 1;
    }

    /**
     * Create or update leave tracker for an employee
     */
    public LeaveTrackerData syncLeaveTrackerForEmployee(UserAccount employee, int sickLeaveBooked, int casualLeaveBooked, int lopBooked) {
        int accrual = calculateCycleAccrual(employee.getJoining());

        Optional<LeaveTrackerData> existing = leaveTrackerRepository.findByEmployeeId(employee.getEmployeeId());
        LeaveTrackerData tracker;

        if (existing.isPresent()) {
            tracker = existing.get();
        } else {
            String employeeName = ((employee.getFirstName() != null ? employee.getFirstName() : "") + " " +
                                  (employee.getLastName() != null ? employee.getLastName() : "")).trim();
            if (employeeName.isEmpty()) {
                employeeName = "Unknown Employee";
            }
        }

        tracker.setSickLeaveAvailable(Math.max(0, accrual - sickLeaveBooked));
        tracker.setCasualLeaveAvailable(Math.max(0, accrual - casualLeaveBooked));
        tracker.setLossOfPayAvailable(0);
        tracker.setSickLeaveBooked(sickLeaveBooked);
        tracker.setCasualLeaveBooked(casualLeaveBooked);
        tracker.setLossOfPayBooked(lopBooked);

        return leaveTrackerRepository.save(tracker);
    }

    /**
     * Sync all employees' leave tracker data
     */
    public void syncAllEmployeeLeaveTrackers() {
        List<UserAccount> employees = userRepository.findAll().stream()
                .filter(user -> user.getRole() != null && user.getRole().equalsIgnoreCase("employee"))
                .toList();

        for (UserAccount employee : employees) {
            if (employee.getEmployeeId() != null) {
                syncLeaveTrackerForEmployee(employee, 0, 0, 0);
            }
        }
    }

    /**
     * Get leave tracker for specific employee
     */
    public LeaveTrackerData getLeaveTrackerForEmployee(String employeeId) {
        Optional<LeaveTrackerData> tracker = leaveTrackerRepository.findByEmployeeId(employeeId);
        if (tracker.isEmpty()) {
            Optional<UserAccount> employee = userRepository.findByEmployeeId(employeeId);
            if (employee.isPresent()) {
                return syncLeaveTrackerForEmployee(employee.get(), 0, 0, 0);
            }
        }
        return tracker.orElse(null);
    }
}
