# LeavePal LMS API Documentation

## Overview
LeavePal is a comprehensive Leave Management System that provides APIs for user authentication, employee management, and leave tracking.

**Base URL:** `http://localhost:8081/api`

**Authentication:** Session-based (login required for most endpoints)

---

## Authentication Endpoints

### 1. User Login
**Endpoint:** `POST /api/auth/login`

**Description:** Authenticate user with credentials and role.

**Request Body:**
```json
{
  "username": "admin",
  "password": "password123",
  "role": "admin"
}
```

**Response Codes:**
- `200` - Login successful, returns user profile
- `401` - Invalid credentials or role
- `500` - Internal server error

**Response (200):**
```json
{
  "username": "admin",
  "role": "admin",
  "employeeId": null,
  "emailId": "admin@leavepal.com",
  "firstName": "System",
  "lastName": "Administrator",
  "department": "IT",
  "designation": "Administrator",
  "reporting": null,
  "location": "Head Office",
  "joining": "2024-01-01",
  "phoneNumber": "+1-555-0000",
  "nationality": "American",
  "bloodGroup": "O+",
  "maritalStatus": "Single",
  "dob": "1980-01-01",
  "personalEmail": "admin@leavepal.com",
  "gender": "Male",
  "address": "123 Admin St, HQ"
}
```

---

## User Management Endpoints

### 2. Get User Profile
**Endpoint:** `GET /api/users/{username}`

**Description:** Retrieve detailed profile information for a specific user.

**Path Parameters:**
- `username` (string, required) - Username of the user

**Response Codes:**
- `200` - Profile retrieved successfully
- `404` - User not found
- `500` - Internal server error

---

### 3. Update User Profile
**Endpoint:** `PUT /api/users/{username}`

**Description:** Update user profile information. For employees, only mutable fields can be updated (immutable fields like employee ID, name, email are preserved).

**Path Parameters:**
- `username` (string, required) - Username of the user to update

**Request Body:** (Only mutable fields for employees)
```json
{
  "phoneNumber": "+1-555-0123",
  "nationality": "American",
  "bloodGroup": "O+",
  "maritalStatus": "Single",
  "dob": "1990-05-15",
  "personalEmail": "john.doe@gmail.com",
  "gender": "Male",
  "address": "123 Main St, New York, NY 10001"
}
```

**Response Codes:**
- `200` - Profile updated successfully
- `404` - User not found
- `500` - Internal server error

---

### 4. Create New User
**Endpoint:** `POST /api/users`

**Description:** Create a new user account. For employees, auto-generates sequential employee ID (LP-001, LP-002, etc.) if not provided, and sets joining date to current date.

**Request Body:**
```json
{
  "username": "john.doe@company.com",
  "password": "password123",
  "role": "employee",
  "employeeId": "LP-003",  // Optional, auto-generated if not provided
  "emailId": "john.doe@company.com",
  "firstName": "John",
  "lastName": "Doe",
  "department": "Engineering",
  "designation": "Software Engineer",
  "reporting": "Jane Smith",
  "location": "New York",
  "joining": "2024-01-15",  // Optional, auto-set to current date
  "phoneNumber": "+1-555-0123",
  "nationality": "American",
  "bloodGroup": "O+",
  "maritalStatus": "Single",
  "dob": "1990-05-15",
  "personalEmail": "john.doe@gmail.com",
  "gender": "Male",
  "address": "123 Main St, New York, NY 10001"
}
```

**Response Codes:**
- `201` - User created successfully
- `409` - Username or Employee ID already exists
- `500` - Internal server error

---

### 5. Get All Users
**Endpoint:** `GET /api/users`

**Description:** Retrieve a list of all users in the system.

**Response Codes:**
- `200` - Users retrieved successfully
- `500` - Internal server error

**Response (200):**
```json
[
  {
    "username": "admin",
    "role": "admin",
    "employeeId": null,
    "emailId": "admin@leavepal.com",
    "firstName": "System",
    "lastName": "Administrator",
    "department": "IT",
    "designation": "Administrator",
    "reporting": null,
    "location": "Head Office",
    "joining": "2024-01-01",
    "phoneNumber": "+1-555-0000",
    "nationality": "American",
    "bloodGroup": "O+",
    "maritalStatus": "Single",
    "dob": "1980-01-01",
    "personalEmail": "admin@leavepal.com",
    "gender": "Male",
    "address": "123 Admin St, HQ"
  },
  {
    "username": "john.doe@company.com",
    "role": "employee",
    "employeeId": "LP-001",
    "emailId": "john.doe@company.com",
    "firstName": "John",
    "lastName": "Doe",
    "department": "Engineering",
    "designation": "Software Engineer",
    "reporting": "Jane Smith",
    "location": "New York",
    "joining": "2024-01-15",
    "phoneNumber": "+1-555-0123",
    "nationality": "American",
    "bloodGroup": "O+",
    "maritalStatus": "Single",
    "dob": "1990-05-15",
    "personalEmail": "john.doe@gmail.com",
    "gender": "Male",
    "address": "123 Main St, New York, NY 10001"
  }
]
```

---

## Data Models

### LoginRequest
```json
{
  "username": "string (required)",
  "password": "string (required)",
  "role": "admin | employee (required)"
}
```

### CreateUserRequest
```json
{
  "username": "string (required)",
  "password": "string (required)",
  "role": "admin | employee (required)",
  "employeeId": "string (optional, auto-generated for employees)",
  "emailId": "string",
  "firstName": "string (required)",
  "lastName": "string",
  "department": "string",
  "designation": "string",
  "reporting": "string",
  "location": "string",
  "joining": "string (optional, auto-set to current date)",
  "phoneNumber": "string",
  "nationality": "string",
  "bloodGroup": "string",
  "maritalStatus": "string",
  "dob": "string",
  "personalEmail": "string",
  "gender": "string",
  "address": "string"
}
```

### UserResponse
```json
{
  "username": "string",
  "role": "admin | employee",
  "employeeId": "string (employees only)",
  "emailId": "string",
  "firstName": "string",
  "lastName": "string",
  "department": "string",
  "designation": "string",
  "reporting": "string",
  "location": "string",
  "joining": "string",
  "phoneNumber": "string",
  "nationality": "string",
  "bloodGroup": "string",
  "maritalStatus": "string",
  "dob": "string",
  "personalEmail": "string",
  "gender": "string",
  "address": "string"
}
```

---

## Business Rules

### Employee ID Generation
- Sequential format: LP-001, LP-002, LP-003, etc.
- Auto-generated for new employees if not provided
- Must be unique across all employees

### Leave Balance Calculation
- 1 Sick Leave + 1 Casual Leave per month
- Accrual period: April to March (financial year)
- Calculated based on joining date
- Balances stored per user in localStorage

### Profile Field Immutability
For employees, the following fields cannot be modified after creation:
- Employee ID
- Username/Email
- First Name
- Last Name
- Department
- Designation
- Reporting Manager
- Location
- Joining Date

Mutable fields (can be updated):
- Phone Number
- Nationality
- Blood Group
- Marital Status
- Date of Birth
- Personal Email
- Gender
- Address

---

## Error Handling

All endpoints return appropriate HTTP status codes:
- `200` - Success
- `201` - Created
- `401` - Unauthorized
- `404` - Not Found
- `409` - Conflict (duplicate data)
- `500` - Internal Server Error

Error responses contain plain text messages describing the issue.

---

## Swagger UI Access

Interactive API documentation is available at:
**URL:** `http://localhost:8081/swagger-ui/index.html`

This provides:
- Interactive API testing
- Request/response examples
- Schema definitions
- Authentication testing