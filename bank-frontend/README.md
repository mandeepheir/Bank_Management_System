# Bank Management System

A backend-focused banking application built with **Java, Spring Boot, Spring Security, and PostgreSQL**. The project provides REST APIs for managing bank accounts and performing common banking operations, with user registration and authentication.

The goal is to build a secure, maintainable banking application while applying practical software engineering concepts.

## Features

- **Account Management** — Create, retrieve, and delete bank accounts.
- **Deposit Operations** — Deposit money into an account.
- **Withdrawal Operations** — Withdraw money from an account.
- **Transaction History** — Retrieve transaction records for an account.
- **User Registration** — Register users with password hashing using BCrypt.
- **Authentication** — Protect API endpoints using Spring Security and HTTP Basic authentication.
- **PostgreSQL Integration** — Persist application data in a relational database.
- **RESTful API** — Expose banking functionality through HTTP endpoints.

> **Development status:** Account ownership enforcement and frontend integration are still in progress.

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend programming |
| Spring Boot | Application framework |
| Spring Security | Authentication and API protection |
| Spring Data JPA | Database persistence |
| Hibernate | ORM and entity mapping |
| PostgreSQL | Relational database |
| Maven | Build and dependency management |
| REST APIs | Client-server communication |
| Angular | Planned frontend integration |

## Architecture

The application follows a layered backend structure:

- **Controller layer:** Handles HTTP requests and API responses.
- **Service layer:** Contains banking business logic.
- **Repository layer:** Communicates with the database through Spring Data JPA.
- **Entity layer:** Represents application data, including accounts, users, and transactions.
- **Security layer:** Configures authentication, password encoding, and endpoint authorization.

## API Endpoints

Base URL: `http://localhost:8080`

### General

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/bank/hello` | Public |
| GET | `/api/bank/info` | Public |

### Authentication

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register` | Public |

Registration request:

```json
{
  "username": "demo_user",
  "password": "StrongPass123!"
}
```

The password must contain at least eight characters. Passwords are hashed using BCrypt before storage.

### Bank Accounts

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/bank/accounts` | Retrieve accounts |
| GET | `/api/bank/accounts/{id}` | Retrieve an account |
| POST | `/api/bank/accounts` | Create an account |
| POST | `/api/bank/accounts/{id}/deposit` | Deposit money |
| POST | `/api/bank/accounts/{id}/withdraw` | Withdraw money |
| GET | `/api/bank/accounts/{id}/transactions` | Retrieve transaction history |
| DELETE | `/api/bank/accounts/{id}` | Delete an account |

**Note:** Account endpoints other than the explicitly public general endpoints require authentication. Account-level ownership enforcement is a work in progress.

## Prerequisites

Install the following before running the application:

- Java Development Kit (JDK) 21
- PostgreSQL
- Git

The project includes Maven Wrapper scripts, so a separate Maven installation is not required.

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/mandeepheir/Bank_Management_System.git
cd Bank_Management_System
```

### 2. Create the database

Open PostgreSQL and run:

```sql
CREATE DATABASE bank_management_db;
```

If the database already exists, do not create it again.

### 3. Configure database credentials

Open `src/main/resources/application.properties` and configure your local PostgreSQL connection:

```properties
spring.application.name=bank-management-system

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/bank_management_db}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.open-in-view=false
```

Set the environment variables `DB_USERNAME` and `DB_PASSWORD` to your PostgreSQL credentials before starting the application.

**Security:** Never commit database passwords, API keys, or other secrets to GitHub. Use environment variables for local configuration and a secrets manager for production deployments.

### 4. Build the application

On Windows PowerShell:

```powershell
.\mvnw.cmd compile
```

On macOS or Linux:

```bash
./mvnw compile
```

### 5. Run the application

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS or Linux:

```bash
./mvnw spring-boot:run
```

The backend runs at:

`http://localhost:8080`

## Authentication

The application currently uses HTTP Basic authentication for protected endpoints.

After registering a user, send the username and password with requests to protected routes. For example, using curl:

```bash
curl -u demo_user:StrongPass123! \
  http://localhost:8080/api/bank/accounts
```

Use test credentials only in examples. Avoid putting real passwords directly into shell commands or source code.

## Testing

The project includes Spring Boot testing dependencies.

Run the test suite with:

Windows:

```powershell
.\mvnw.cmd test
```

macOS or Linux:

```bash
./mvnw test
```

## Roadmap

Planned improvements include:

- Enforce account ownership for every account operation.
- Complete the database migration for existing account records.
- Add automated unit and integration tests.
- Add input validation and consistent API error responses.
- Integrate the Angular frontend with the backend.
- Evaluate JWT-based authentication for frontend integration.
- Add API documentation using OpenAPI/Swagger.
- Improve deployment configuration and production security.

## Engineering Concepts Demonstrated

- Object-oriented programming with Java
- REST API design
- Layered application architecture
- Relational database modeling
- ORM with JPA and Hibernate
- Authentication and password hashing
- HTTP status codes and API authorization
- Build and dependency management with Maven

## Author

**Mandeep Singh**

GitHub: [@mandeepheir](https://github.com/mandeepheir)

Repository: [Bank Management System](https://github.com/mandeepheir/Bank_Management_System)

---

*This project is being developed as part of my continued learning in backend engineering, secure API development, and full-stack application development.*
