# Payment Gateway Service

A Spring Boot application for handling payment gateway integrations.

## Tech Stack

- Java 21
- Spring Boot 3.5.0
- MySQL
- Liquibase
- OkHttp 4.12.0
- Apache HttpClient 4.5.14
- Lombok
- Swagger (OpenAPI)

## Prerequisites

- JDK 21
- Maven
- Docker & Docker Compose

## Getting Started

1. Clone the repository
2. Configure MySQL connection in `application.properties` (for local run)
3. Run database migrations (auto-applied on app start via Liquibase)
4. Run the application:
```bash
./mvnw spring-boot:run
```

## Running with Docker Compose

1. Build the application JAR:
   ```bash
   ./mvnw clean package -DskipTests
   ```
2. Build and start all services (Spring Boot app + MySQL):
   ```bash
   docker-compose up --build
   ```
3. The app will be available at [http://localhost:8080](http://localhost:8080)
4. The MySQL database will be available at port 3308 on your host.

## Features

- RESTful API endpoints for payment processing
- MySQL integration for data persistence
- Database versioning with Liquibase
- HTTP client support for external API calls
- Input validation
- API documentation with Swagger UI (`/swagger-ui.html`)

## Project Structure

- `src/main/java`: Java source files
- `src/main/resources`: Configuration files and Liquibase changelogs
- `src/test`: Test files

## Dependencies

- Spring Boot Starter Web
- Spring Boot Starter Validation
- Spring Boot Starter Data JPA
- MySQL Connector/J
- Liquibase Core
- OkHttp
- Apache HttpClient
- Lombok
- Springdoc OpenAPI 