# Medimart Backend

Medimart Backend is the core backend system for the Medimart application, a platform designed to streamline the management of medicines, orders, notifications, and payments. It provides RESTful APIs for seamless interaction between the client and server, ensuring a smooth user experience.

---

## Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)

---

## Features
- Secure user authentication and authorization.
- Robust admin features for managing notifications, analytics, and users.
- API endpoints for managing medicines, orders, carts, and payments.
- PDF invoice generation.
- WebSocket integration for real-time updates.
- Support for multi-environment configurations (local and production).

---

## Tech Stack
- **Java** (Primary language)
- **Spring Boot** (REST API development, security, and configuration)
- **Hibernate/JPA** (Database interaction)
- **Maven** (Dependency management)
- **MySQL** (or any other relational database)
- **WebSockets** (Real-time communication)

---
## Project Structure
```plaintext
.
├── mvnw, pom.xml           # Maven wrapper and configuration
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.medimart
│   │   │       ├── MedimartBackendApplication.java  # Main entry point
│   │   │       ├── config                           # App configurations
│   │   │       ├── controller                       # REST API endpoints
│   │   │       ├── dto                              # Data transfer objects
│   │   │       ├── model                            # Entity definitions
│   │   │       ├── repository                       # DB access interfaces
│   │   │       └── service                          # Business logic
│   └── resources
│       ├── application.properties                   # Configuration files
│       ├── logo_medimart.png                        # Branding logo
                                            
```
---
## Getting Started

### Prerequisites
Ensure you have the following installed:
- Java 17 or later
- Maven
- MySQL or any supported relational database

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Jahidul183019/medimart-backend.git
   ```
2. Navigate to the project directory:
   ```bash
   cd medimart-backend
   ```
3. Configure the `application.properties` file:
   - Set database connection parameters.
   - Configure any additional environment-specific properties.

4. Build the project:
   ```bash
   mvn clean install
   ```

5. Run the application:
   ```bash
   mvn spring-boot:run
   ```

---
