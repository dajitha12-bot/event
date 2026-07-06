# Spring Boot Event Management System

A simple, beginner-friendly Event Management System built with Spring Boot 3, Thymeleaf, SQLite, and Spring Security.

## 🧰 Technology Stack
- **Java 17**
- **Spring Boot 3.3.0**
- **Maven**
- **Spring Security** (Session-based, BCrypt encryption)
- **Spring Data JPA**
- **SQLite**
- **Thymeleaf**
- **Bootstrap 5** (Sky Blue professional UI theme)

## 📁 Project Features
- **User Authentication**: Secure Sign Up and Log In.
- **Event Management**: Create, view, edit, and delete events.
- **Security Validation**:
  - Future Date validation for events.
  - Required field validations on all form submissions.
  - Ownership protection: Users can only see, modify, or delete their own events.

## 🚀 How to Run the Project

### Prerequisites
Make sure you have the following installed:
- **Java JDK 17**
- **Maven 3.x**

### Run Commands
Navigate to the project root directory in your terminal and execute:

```bash
# Clean and build the application
mvn clean install

# Launch the Spring Boot server
mvn spring-boot:run
```

Once started, the application will be hosted locally at:
👉 **[http://localhost:8080/](http://localhost:8080/)**

## 🗄️ Database Configurations
The project uses SQLite for storage. 
A file database named `eventapp.db` will be auto-generated in the project root directory during the first run.
- **Users table**: Stores User ID, Full Name, Email, and encrypted Passwords.
- **Events table**: Stores Event details linked to the user owner.

## 🎨 UI Colors
- **Primary Highlights / Brand**: `#87CEEB` (Sky Blue)
- **Background Theme**: `#E0F7FF` (Soft Pale Blue)
- **Content Cards**: White elements with subtle shadows and rounded borders (`14px`).
