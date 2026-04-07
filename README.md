# Banking App APIs

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

A clean **RESTful Banking Backend** built with **Spring Boot 3** and **Java 21**. The project follows a standard layered architecture and currently supports user-related operations with email notifications.

---

## ✨ Features

- User Registration & Authentication
- Email alerts on registration
- Role-based entities (e.g., `Role.java`)
- DTOs for clean request/response handling
- Proper separation of concerns (Controller, Service, Repository, Entity, DTO)
- Configurable application settings

---

## 🛠 Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.x
- **Build Tool**: Maven (with Maven Wrapper)
- **Database**: Configurable (H2 / MySQL / PostgreSQL)
- **Email**: Spring Mail integration

---

## 📁 Project Structure

```bash
banking-app-apis/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/banking/banking_app_apis/
│       │       ├── BankingAppApisApplication.java
│       │       ├── config/              # Configuration classes
│       │       ├── controller/          # REST Controllers
│       │       ├── dto/                 # Data Transfer Objects
│       │       ├── entity/              # JPA Entities (User, Role, etc.)
│       │       ├── repository/          # Spring Data JPA Repositories
│       │       ├── service/
│       │       │   └── impl/            # Service Implementations
│       │       └── utils/               # Utility classes
│       └── resources/                   # (Currently minimal/empty)
├── application.properties               # ← Located in root folder
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .gitignore
└── README.md
