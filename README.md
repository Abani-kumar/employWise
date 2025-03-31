# EmployWise Employee Management System

## Project Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven
- MongoDB
- Gmail Account for Email Notifications

### Database Setup
1. go to application.properties and give value ```spring.data.mongodb.uri```
  your mongodburl 

### Email Configuration
1. Create a new Gmail account
2. Generate an App Password
3. Update spring.mail.username and spring.mail.password in application.properties

### Running the Application
```bash
mvn clean install
mvn spring-boot:run
```

## API Documentation

### 1. Add Employee
- **URL**: `/api/employees`
- **Method**: POST
- **Request Body**:
```json
{
    "employeeName": "Abani",
    "phoneNumber": "+919876543210",
    "email": "abani@example.com",
    "reportsTo": "manager_id",
    "profileImageUrl": "https://example.com/profile.jpg"
}
```

### 2. Get All Employees
- **URL**: `/api/employees`
- **Method**: GET

### 3. Get Paginated Employees
- **URL**: `/api/employees/paginated`
- **Method**: GET
- **Query Params**: 
  - `page` (default: 0)
  - `size` (default: 10)
  - `sortBy` (default: "employeeName")

### 4. Update Employee
- **URL**: `/api/employees/{id}`
- **Method**: PUT
- **Request Body**: Same as Add Employee

### 5. Delete Employee
- **URL**: `/api/employees/{id}`
- **Method**: DELETE

### 6. Get Nth Level Manager
- **URL**: `/api/employees/{employeeId}/manager`
- **Method**: GET
- **Query Param**: `level`

## Deployment
Hosted on: [Provide Hosting URL if applicable]
