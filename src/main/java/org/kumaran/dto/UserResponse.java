package org.kumaran.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.kumaran.entity.UserAccount;

@Schema(description = "User profile response")
public class UserResponse {
    @Schema(description = "Username/login identifier", example = "john.doe@company.com")
    private String username;

    @Schema(description = "JWT token for authenticated requests", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Relative redirect URL after successful login", example = "/dashboard.html")
    private String redirectUrl;

    @Schema(description = "User role", example = "employee", allowableValues = {"admin", "manager", "employee"})
    private String role;

    @Schema(description = "Employee ID (for employees only)", example = "LP-001")
    private String employeeId;

    @Schema(description = "Official email address", example = "john.doe@company.com")
    private String emailId;

    @Schema(description = "First name", example = "John")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "Department name", example = "Engineering")
    private String department;

    @Schema(description = "Job designation", example = "Software Engineer")
    private String designation;

    @Schema(description = "Reporting authority employee ID", example = "LP-001")
    private String reportingEmployeeId;

    @Schema(description = "Reporting manager username", example = "manager@company.com")
    private String reportingUsername;

    @Schema(description = "Reporting manager email", example = "manager@company.com")
    private String reportingEmail;

    @Schema(description = "Reporting manager display name", example = "Manager Name")
    private String reportingName;

    @Schema(description = "Legacy reporting label for compatibility", example = "LP-001")
    private String reporting;

    @Schema(description = "Office location", example = "New York")
    private String location;

    @Schema(description = "Joining date", example = "2024-01-15")
    private String joining;

    @Schema(description = "Phone number", example = "+1-555-0123")
    private String phoneNumber;

    @Schema(description = "Nationality", example = "American")
    private String nationality;

    @Schema(description = "Blood group", example = "O+")
    private String bloodGroup;

    @Schema(description = "Marital status", example = "Single")
    private String maritalStatus;

    @Schema(description = "Date of birth", example = "1990-05-15")
    private String dob;

    @Schema(description = "Personal email address", example = "john.doe@gmail.com")
    private String personalEmail;

    @Schema(description = "Gender", example = "Male")
    private String gender;

    @Schema(description = "Residential address", example = "123 Main St, New York, NY 10001")
    private String address;

    @Schema(description = "Whether the user has requested a password reset", example = "false")
    private boolean passwordResetRequested;

    @Schema(description = "Password reset request timestamp", example = "2026-04-10T10:15:30Z")
    private String passwordResetRequestedAt;

    @Schema(description = "Whether login is blocked until password is changed", example = "false")
    private boolean forcePasswordChange;

    @Schema(description = "Temporary password issue timestamp", example = "2026-04-10T10:20:00Z")
    private String temporaryPasswordIssuedAt;

    @Schema(description = "Response message for operations like profile updates", example = "Profile updated successfully")
    private String message;

    public static UserResponse from(UserAccount user) {
        UserResponse response = new UserResponse();
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setEmployeeId(user.getEmployeeId());
        response.setToken(null);
        response.setRedirectUrl(null);
        response.setEmailId(user.getEmailId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setDepartment(user.getDepartment());
        response.setDesignation(user.getDesignation());
        response.setReportingEmployeeId(user.getReportingEmployeeId());
        response.setLocation(user.getLocation());
        response.setJoining(user.getJoining());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setNationality(user.getNationality());
        response.setBloodGroup(user.getBloodGroup());
        response.setMaritalStatus(user.getMaritalStatus());
        response.setDob(user.getDob());
        response.setPersonalEmail(user.getPersonalEmail());
        response.setGender(user.getGender());
        response.setAddress(user.getAddress());
        response.setPasswordResetRequested(user.isPasswordResetRequested());
        response.setPasswordResetRequestedAt(user.getPasswordResetRequestedAt());
        response.setForcePasswordChange(user.isForcePasswordChange());
        response.setTemporaryPasswordIssuedAt(user.getTemporaryPasswordIssuedAt());
        return response;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getReportingEmployeeId() {
        return reportingEmployeeId;
    }

    public void setReportingEmployeeId(String reportingEmployeeId) {
        this.reportingEmployeeId = reportingEmployeeId;
    }

    public String getReportingUsername() {
        return reportingUsername;
    }

    public void setReportingUsername(String reportingUsername) {
        this.reportingUsername = reportingUsername;
    }

    public String getReportingEmail() {
        return reportingEmail;
    }

    public void setReportingEmail(String reportingEmail) {
        this.reportingEmail = reportingEmail;
    }

    public String getReportingName() {
        return reportingName;
    }

    public void setReportingName(String reportingName) {
        this.reportingName = reportingName;
    }

    public String getReporting() {
        return reporting;
    }

    public void setReporting(String reporting) {
        this.reporting = reporting;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getJoining() {
        return joining;
    }

    public void setJoining(String joining) {
        this.joining = joining;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPersonalEmail() {
        return personalEmail;
    }

    public void setPersonalEmail(String personalEmail) {
        this.personalEmail = personalEmail;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isPasswordResetRequested() {
        return passwordResetRequested;
    }

    public void setPasswordResetRequested(boolean passwordResetRequested) {
        this.passwordResetRequested = passwordResetRequested;
    }

    public String getPasswordResetRequestedAt() {
        return passwordResetRequestedAt;
    }

    public void setPasswordResetRequestedAt(String passwordResetRequestedAt) {
        this.passwordResetRequestedAt = passwordResetRequestedAt;
    }

    public boolean isForcePasswordChange() {
        return forcePasswordChange;
    }

    public void setForcePasswordChange(boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
    }

    public String getTemporaryPasswordIssuedAt() {
        return temporaryPasswordIssuedAt;
    }

    public void setTemporaryPasswordIssuedAt(String temporaryPasswordIssuedAt) {
        this.temporaryPasswordIssuedAt = temporaryPasswordIssuedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}


