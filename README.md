# 🎓 Event Management System — Spring Boot

A full-stack college Event Management System with three dashboards: **Student**, **Faculty**, and **Admin**.

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- VS Code with "Extension Pack for Java" or IntelliJ IDEA

### Run the Application

```bash
# Clone / unzip the project, then:
cd EventManagementSystem

# Build & run
mvn spring-boot:run
```

Open your browser at: **http://localhost:8080**

---

## 🔐 Demo Credentials

| Role    | Email / Login             | Password    |
|---------|---------------------------|-------------|
| Student | alice@student.edu         | student123  |
| Student | bob@student.edu           | student123  |
| Faculty | sharma@college.edu        | faculty123  |
| Faculty | patel@college.edu         | faculty123  |
| Admin   | (password only)           | admin123    |

---

## 📁 Project Structure

```
EventManagementSystem/
├── src/main/java/com/eventmanagement/
│   ├── EventManagementApplication.java   ← Main entry point
│   ├── controller/
│   │   ├── HomeController.java
│   │   ├── StudentController.java
│   │   ├── FacultyController.java
│   │   └── AdminController.java
│   ├── model/
│   │   ├── Student.java
│   │   ├── Faculty.java
│   │   ├── Event.java
│   │   ├── Registration.java
│   │   └── Payment.java
│   ├── repository/
│   │   ├── StudentRepository.java
│   │   ├── FacultyRepository.java
│   │   ├── EventRepository.java
│   │   ├── RegistrationRepository.java
│   │   └── PaymentRepository.java
│   └── service/
│       └── EventService.java
├── src/main/resources/
│   ├── application.properties
│   ├── static/css/main.css
│   └── templates/
│       ├── index.html                    ← Home page
│       ├── student/                      ← Student pages
│       ├── faculty/                      ← Faculty pages
│       └── admin/                        ← Admin pages
└── pom.xml
```

---

## ✨ Features

### 🎓 Student Dashboard
- Register / Login
- Browse approved events with real-time seat availability
- Register for events with payment (Credit Card, Debit Card, UPI, Net Banking, Cash)
- View registration receipt with Transaction ID
- Submit star ratings + feedback for attended events
- View payment history with all transactions

### 👨‍🏫 Faculty Dashboard
- Register / Login
- Create events with venue, time, fee, and description
- Automatic venue conflict detection
- View registered students per event
- Earnings report with event-wise revenue breakdown

### 🛡️ Admin Dashboard
- Secure password-only login
- Approve or reject pending events
- View all events, registrations and feedback
- Complete financial summary with revenue analytics

---

## 🗄️ Database

Uses **H2 in-memory database** (auto-configured). Data resets on restart.

- H2 Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:eventdb`
- Username: `sa` / Password: (empty)

To use **MySQL** instead, update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/eventdb
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update
```

And add MySQL dependency to `pom.xml`:
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## 🛠️ VS Code Setup

1. Install **Extension Pack for Java** (Microsoft)
2. Open the `EventManagementSystem` folder in VS Code
3. VS Code will auto-detect the Maven project
4. Press **F5** or run `mvn spring-boot:run` in the terminal
