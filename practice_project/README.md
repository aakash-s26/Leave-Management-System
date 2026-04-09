# LeavePal LMS API Documentation

## Overview
LeavePal is a comprehensive Leave Management System built with Spring Boot that provides REST APIs for user authentication, employee management, and leave tracking.

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL database

### Running the Application
```bash
cd practice_project
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

## 📚 API Documentation

### Swagger UI (Interactive Documentation)
Access the interactive API documentation at:
**URL:** `http://localhost:8081/swagger-ui/index.html`

Features:
- ✅ Interactive API testing
- ✅ Request/response examples
- ✅ Schema definitions
- ✅ Authentication testing
- ✅ Try-it-out functionality

### OpenAPI Specification
The raw OpenAPI 3.0 specification is available at:
**URL:** `http://localhost:8081/v3/api-docs`

## 🔗 API Endpoints

### Authentication
- `POST /api/auth/login` - User login with credentials and role

### User Management
- `GET /api/users/{username}` - Get user profile
- `PUT /api/users/{username}` - Update user profile
- `POST /api/users` - Create new user
- `GET /api/users` - Get all users

## 📋 Response Codes Reference

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 401 | Unauthorized |
| 404 | Not Found |
| 409 | Conflict (duplicate data) |
| 500 | Internal Server Error |

## 🔐 Authentication

The API uses session-based authentication. Login via `/api/auth/login` to obtain user session.

### Sample Login Request
```json
{
  "username": "admin",
  "password": "password123",
  "role": "admin"
}
```

## 📄 Data Models

### User Roles
- `admin` - System administrator
- `employee` - Regular employee

### Employee ID Format
- Sequential format: `LP-001`, `LP-002`, `LP-003`, etc.
- Auto-generated for new employees

### Leave Balance Calculation
- 1 Sick Leave + 1 Casual Leave per month
- Accrual period: April to March (financial year)
- Calculated based on joining date

## 🧪 Testing the APIs

### Using Swagger UI
1. Open `http://localhost:8081/swagger-ui/index.html`
2. Expand any endpoint
3. Click "Try it out"
4. Fill in the parameters
5. Click "Execute"

### Using cURL Examples

#### Login as Admin
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123",
    "role": "admin"
  }'
```

#### Get All Users
```bash
curl -X GET http://localhost:8081/api/users
```

#### Create New Employee
```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe@company.com",
    "password": "password123",
    "role": "employee",
    "firstName": "John",
    "lastName": "Doe",
    "department": "Engineering",
    "designation": "Software Engineer",
    "emailId": "john.doe@company.com"
  }'
```

## 📁 Project Structure

```
practice_project/
├── src/main/java/org/kumaran/
│   ├── config/
│   │   └── OpenApiConfig.java          # Swagger configuration
│   ├── controller/
│   │   └── AuthController.java         # REST API endpoints
│   ├── model/
│   │   ├── UserAccount.java            # JPA entity
│   │   ├── UserResponse.java           # API response DTO
│   │   ├── LoginRequest.java           # Login request DTO
│   │   └── CreateUserRequest.java      # User creation DTO
│   ├── repository/
│   │   └── UserAccountRepository.java  # JPA repository
│   └── Main.java                       # Application entry point
├── src/main/resources/
│   ├── static/                         # Frontend files
│   └── application.properties          # Configuration
├── API_DOCUMENTATION.md                # Detailed API docs
└── pom.xml                             # Maven dependencies
```

## 🛠️ Technologies Used

- **Backend:** Spring Boot 3.2.3, Spring Data JPA
- **Database:** PostgreSQL
- **Security:** BCrypt password encoding
- **Documentation:** SpringDoc OpenAPI 3.0 (Swagger)
- **Frontend:** Vanilla HTML/CSS/JavaScript
- **Build Tool:** Maven

## 📞 Support

For API-related questions or issues:
- Check the Swagger UI at `http://localhost:8081/swagger-ui/index.html`
- Review the detailed documentation in `API_DOCUMENTATION.md`
- Test endpoints directly using the Swagger interface

## 🔄 Business Rules

### Employee Profile Fields
**Immutable (cannot be changed after creation):**
- Employee ID, Username, Email, Name, Department, Designation, Reporting Manager, Location, Joining Date

**Mutable (can be updated):**
- Phone, Nationality, Blood Group, Marital Status, DOB, Personal Email, Gender, Address

### Leave System
- Monthly accrual: 1 Sick + 1 Casual leave per month
- Financial year: April 1st to March 31st
- Excess leave applications are booked as LOP (Loss of Pay)
- Balances are calculated client-side based on joining date

---

**API Documentation Generated:** April 8, 2026
**Version:** 1.0.0