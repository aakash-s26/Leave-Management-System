package org.kumaran.model;

import jakarta.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "leave_tracker")
@Schema(description = "Leave tracker data for employees")
public class LeaveTrackerData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Employee ID", example = "LP-001")
    private String employeeId;

    @Column(nullable = false)
    @Schema(description = "Employee full name", example = "John Doe")
    private String employeeName;

    @Schema(description = "Job role/designation", example = "Software Engineer")
    private String role;

    @Schema(description = "Department", example = "Engineering")
    private String department;

    @Schema(description = "Date of joining", example = "2024-12-01")
    private String joiningDate;

    @Column(nullable = false)
    @Schema(description = "Sick leave available in current cycle", example = "4")
    private Integer sickLeaveAvailable = 0;

    @Column(nullable = false)
    @Schema(description = "Casual leave available in current cycle", example = "4")
    private Integer casualLeaveAvailable = 0;

    @Column(nullable = false)
    @Schema(description = "Loss of pay available", example = "0")
    private Integer lossOfPayAvailable = 0;

    @Column(nullable = false)
    @Schema(description = "Sick leave booked", example = "0")
    private Integer sickLeaveBooked = 0;

    @Column(nullable = false)
    @Schema(description = "Casual leave booked", example = "0")
    private Integer casualLeaveBooked = 0;

    @Column(nullable = false)
    @Schema(description = "Loss of pay booked", example = "0")
    private Integer lossOfPayBooked = 0;

    @Column(nullable = false, updatable = false)
    @Schema(description = "Created timestamp")
    private Long createdAt = System.currentTimeMillis();

    @Column(nullable = false)
    @Schema(description = "Updated timestamp")
    private Long updatedAt = System.currentTimeMillis();

    // Constructors
    public LeaveTrackerData() {
    }

    public LeaveTrackerData(String employeeId, String employeeName, String role, String department, String joiningDate) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.role = role;
        this.department = department;
        this.joiningDate = joiningDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(String joiningDate) {
        this.joiningDate = joiningDate;
    }

    public Integer getSickLeaveAvailable() {
        return sickLeaveAvailable;
    }

    public void setSickLeaveAvailable(Integer sickLeaveAvailable) {
        this.sickLeaveAvailable = sickLeaveAvailable;
        this.updatedAt = System.currentTimeMillis();
    }

    public Integer getCasualLeaveAvailable() {
        return casualLeaveAvailable;
    }

    public void setCasualLeaveAvailable(Integer casualLeaveAvailable) {
        this.casualLeaveAvailable = casualLeaveAvailable;
        this.updatedAt = System.currentTimeMillis();
    }

    public Integer getLossOfPayAvailable() {
        return lossOfPayAvailable;
    }

    public void setLossOfPayAvailable(Integer lossOfPayAvailable) {
        this.lossOfPayAvailable = lossOfPayAvailable;
        this.updatedAt = System.currentTimeMillis();
    }

    public Integer getSickLeaveBooked() {
        return sickLeaveBooked;
    }

    public void setSickLeaveBooked(Integer sickLeaveBooked) {
        this.sickLeaveBooked = sickLeaveBooked;
        this.updatedAt = System.currentTimeMillis();
    }

    public Integer getCasualLeaveBooked() {
        return casualLeaveBooked;
    }

    public void setCasualLeaveBooked(Integer casualLeaveBooked) {
        this.casualLeaveBooked = casualLeaveBooked;
        this.updatedAt = System.currentTimeMillis();
    }

    public Integer getLossOfPayBooked() {
        return lossOfPayBooked;
    }

    public void setLossOfPayBooked(Integer lossOfPayBooked) {
        this.lossOfPayBooked = lossOfPayBooked;
        this.updatedAt = System.currentTimeMillis();
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
