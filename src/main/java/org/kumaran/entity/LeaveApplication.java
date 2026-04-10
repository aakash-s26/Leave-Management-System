package org.kumaran.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "leave_application")
public class LeaveApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String employeeId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String emailId;

    private String reportingManagerId;
    private String reportingManagerUsername;
    private String reportingManagerEmail;
    private String reportingManagerName;

    @Column(nullable = false)
    private String employeeName;

    @Column(nullable = false)
    private String leaveType;

    @Column(nullable = false)
    private String fromDate;

    @Column(nullable = false)
    private String toDate;

    @Column(nullable = false)
    private Integer duration;

    private String reason;

    private String sickAttachmentName;

    @Column(columnDefinition = "TEXT")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String sickAttachmentData;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(nullable = false)
    private String appliedDate;

    private String reviewedAt;
    private String reviewedBy;
    private String rejectionReason;
    private String managerComment;

    @Column(nullable = false, updatable = false)
    private Long createdAt = System.currentTimeMillis();

    @Column(nullable = false)
    private Long updatedAt = System.currentTimeMillis();

    @PreUpdate
    public void touch() {
        this.updatedAt = System.currentTimeMillis();
    }

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getReportingManagerId() {
        return reportingManagerId;
    }

    public void setReportingManagerId(String reportingManagerId) {
        this.reportingManagerId = reportingManagerId;
    }

    public String getReportingManagerUsername() {
        return reportingManagerUsername;
    }

    public void setReportingManagerUsername(String reportingManagerUsername) {
        this.reportingManagerUsername = reportingManagerUsername;
    }

    public String getReportingManagerEmail() {
        return reportingManagerEmail;
    }

    public void setReportingManagerEmail(String reportingManagerEmail) {
        this.reportingManagerEmail = reportingManagerEmail;
    }

    public String getReportingManagerName() {
        return reportingManagerName;
    }

    public void setReportingManagerName(String reportingManagerName) {
        this.reportingManagerName = reportingManagerName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSickAttachmentName() {
        return sickAttachmentName;
    }

    public void setSickAttachmentName(String sickAttachmentName) {
        this.sickAttachmentName = sickAttachmentName;
    }

    public String getSickAttachmentData() {
        return sickAttachmentData;
    }

    public void setSickAttachmentData(String sickAttachmentData) {
        this.sickAttachmentData = sickAttachmentData;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(String appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(String reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getManagerComment() {
        return managerComment;
    }

    public void setManagerComment(String managerComment) {
        this.managerComment = managerComment;
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


