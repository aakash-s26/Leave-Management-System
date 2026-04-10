package org.kumaran.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for creating a new user")
public class CreateUserRequest {
    @Schema(description = "Username for login (email for employees)", example = "john.doe@company.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "User password", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "User role", example = "employee", allowableValues = {"admin", "manager", "employee"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    @Schema(description = "Employee ID (auto-generated if not provided for employees)", example = "LP-001")
    private String employeeId;

    @Schema(description = "Official email address", example = "john.doe@company.com")
    private String emailId;

    @Schema(description = "First name", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "Department name", example = "Engineering")
    private String department;

    @Schema(description = "Job designation", example = "Software Engineer")
    private String designation;

    @Schema(description = "Reporting authority employee ID", example = "LP-001")
    private String reportingEmployeeId;

    @Schema(description = "Office location", example = "New York")
    private String location;

    @Schema(description = "Joining date (auto-set to current date if not provided)", example = "2024-01-15")
    private String joining;

    @Schema(description = "Phone number", example = "+1-555-0123")
    private String phoneNumber;

    @Schema(description = "Nationality", example = "American")
    private String nationality;

    @Schema(description = "Blood group", example = "O+")
    private String bloodGroup;

    @Schema(description = "Marital status", example = "Single", allowableValues = {"Single", "Married", "Divorced", "Widowed"})
    private String maritalStatus;

    @Schema(description = "Date of birth", example = "1990-05-15")
    private String dob;

    @Schema(description = "Personal email address", example = "john.doe@gmail.com")
    private String personalEmail;

    @Schema(description = "Gender", example = "Male", allowableValues = {"Male", "Female", "Other"})
    private String gender;

    @Schema(description = "Residential address", example = "123 Main St, New York, NY 10001")
    private String address;

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}