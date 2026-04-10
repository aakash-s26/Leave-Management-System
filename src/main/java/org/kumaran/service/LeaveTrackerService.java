package org.kumaran.service;

import org.kumaran.entity.LeaveTrackerData;
import org.kumaran.entity.UserAccount;
import org.kumaran.entity.LeaveApplication;
import org.kumaran.repository.LeaveApplicationRepository;
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
    private final LeaveApplicationRepository leaveApplicationRepository;

    public LeaveTrackerService(LeaveTrackerRepository leaveTrackerRepository,
                               UserAccountRepository userRepository,
                               LeaveApplicationRepository leaveApplicationRepository) {
        this.leaveTrackerRepository = leaveTrackerRepository;
        this.userRepository = userRepository;
        this.leaveApplicationRepository = leaveApplicationRepository;
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
            tracker = new LeaveTrackerData(
                    employee.getEmployeeId(),
                    employeeName,
                    employee.getRole(),
                    employee.getDepartment(),
                    employee.getJoining()
            );
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
         * Sync all workforce leave tracker data (employees and managers)
     */
    public void syncAllEmployeeLeaveTrackers() {
        List<UserAccount> employees = userRepository.findAll().stream()
            .filter(user -> user.getRole() != null &&
                (user.getRole().equalsIgnoreCase("employee") || user.getRole().equalsIgnoreCase("manager")))
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
        Optional<UserAccount> employee = userRepository.findByEmployeeId(employeeId);
        if (employee.isEmpty()) {
            return null;
        }
        return recalculateLeaveTrackerForEmployee(employee.get());
    }

    public LeaveTrackerData recalculateLeaveTrackerForEmployee(UserAccount employee) {
        if (employee == null || employee.getEmployeeId() == null) {
            return null;
        }

        int accrual = calculateCycleAccrual(employee.getJoining());
        int sickBooked = 0;
        int casualBooked = 0;
        int lopBooked = 0;

        List<LeaveApplication> applications = leaveApplicationRepository
                .findByEmployeeIdOrUsernameOrEmailIdOrderByCreatedAtDesc(
                        employee.getEmployeeId(),
                        employee.getUsername(),
                        employee.getEmailId()
                );

        applications.sort((a, b) -> {
            long aCreatedAt = a.getCreatedAt() == null ? 0L : a.getCreatedAt();
            long bCreatedAt = b.getCreatedAt() == null ? 0L : b.getCreatedAt();
            return Long.compare(aCreatedAt, bCreatedAt);
        });

        for (LeaveApplication app : applications) {
            if (!"approved".equalsIgnoreCase(app.getStatus())) {
                continue;
            }

            int duration = app.getDuration() == null ? 0 : app.getDuration();
            String leaveType = app.getLeaveType() == null ? "" : app.getLeaveType().trim().toLowerCase();

            if (leaveType.equals("lop")) {
                lopBooked += duration;
            } else if (leaveType.equals("sick")) {
                int available = Math.max(0, accrual - sickBooked);
                int used = Math.min(duration, available);
                sickBooked += used;
                lopBooked += duration - used;
            } else if (leaveType.equals("casual")) {
                int available = Math.max(0, accrual - casualBooked);
                int used = Math.min(duration, available);
                casualBooked += used;
                lopBooked += duration - used;
            }
        }

        return syncLeaveTrackerForEmployee(employee, sickBooked, casualBooked, lopBooked);
    }

    public LeaveTrackerData updateLeaveTrackerBookingOnApproval(UserAccount employee) {
        if (employee == null || employee.getEmployeeId() == null) {
            return null;
        }
        return recalculateLeaveTrackerForEmployee(employee);
    }
}

