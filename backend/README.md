# 10xDevs Cards Backend

A Spring Boot backend application for the 10xDevs Cards project, implementing Domain-Driven Design (DDD) with hexagonal architecture and CQRS pattern.

## Table of Contents

- [Project Description](#project-description)
- [Tech Stack](#tech-stack)
- [Getting Started Locally](#getting-started-locally)
- [Available Scripts](#available-scripts)
- [Project Scope](#project-scope)
- [Project Status](#project-status)
- [License](#license)

## Project Description

This is a robust backend API built with Spring Boot 3.5.7 that serves as the foundation for the 10xDevs Cards project. The application follows modern architectural patterns including Domain-Driven Design (DDD), hexagonal architecture, and Command Query Responsibility Segregation (CQRS) to ensure maintainability, scalability, and clear separation of concerns.

The backend provides user management, authentication services, and forms the core infrastructure for card-related functionality with JWT-based security and PostgreSQL persistence.

## Tech Stack

### Core Technologies
- **Java 21** - Programming language
- **Spring Boot 3.5.7** - Application framework
- **PostgreSQL** - Primary database
- **Maven** - Build tool and dependency management

### Key Frameworks & Libraries
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence layer
- **JWT (JJWT 0.11.5)** - Token-based authentication
- **Pipelinr 0.11** - CQRS implementation
- **Liquibase** - Database schema management
- **MapStruct 1.5.5** - Type-safe mapping between layers
- **Lombok 1.18.42** - Boilerplate code reduction

### Development Tools
- **Spring Boot Actuator** - Application monitoring
- **Spring Boot Validation** - Input validation
- **Spring Security Test** - Security testing utilities

## Getting Started Locally

### Prerequisites
- Java 21 or higher
- Docker and Docker Compose
- Maven (or use the included Maven wrapper)

### Database Setup
Start the PostgreSQL database using Docker Compose:
```bash
docker compose up postgres
```

**Database Configuration:**
- Database name: `tenx_cards`
- URL: `jdbc:postgresql://localhost:5432/tenx_cards`
- Username: `postgres` (configurable via `DB_USERNAME` env var)
- Password: `password` (configurable via `DB_PASSWORD` env var)

### Running the Application
1. Clone the repository
2. Start the database (see above)
3. Run the application:
```bash
./mvnw spring-boot:run
```

The application will start on port 8080 by default.

### Available Endpoints
- **Public endpoints:**
  - `POST /users/register` - User registration
  - `POST /auth/login` - User authentication
- **CORS configured for:** `http://localhost:8005`

## Available Scripts

### Building and Running
```bash
# Compile the project
./mvnw clean compile

# Run the application (starts on port 8080)
./mvnw spring-boot:run

# Build JAR file
./mvnw clean package

# Full build with tests
./mvnw clean install
```

### Testing
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=CardsApplicationTests

# Run specific test method
./mvnw test -Dtest=CardsApplicationTests#contextLoads
```

## Project Scope

### Current Features
- **User Management:** Complete user registration and authentication system
- **JWT Authentication:** Stateless authentication with JWT tokens
- **Security Configuration:** Comprehensive Spring Security setup with CORS
- **Database Management:** Liquibase-based schema management
- **CQRS Implementation:** Separated command and query responsibilities

### Architecture Highlights
- **Domain-Driven Design:** Clear bounded contexts and domain modeling
- **Hexagonal Architecture:** Separation of business logic from infrastructure
- **CQRS Pattern:** Commands for write operations, queries for read operations
- **Immutable Domain Entities:** Domain entities with factory methods and snapshots
- **Type-Safe Mapping:** MapStruct-generated mappings between architectural layers

### Planned Features
- Card management functionality
- Enhanced user roles and permissions
- API documentation integration
- Additional business logic implementation

## Project Status

=§ **Development Phase** - Version 0.0.1-SNAPSHOT

The project is currently in active development with core authentication and user management features implemented. The architectural foundation is solid and ready for expansion with additional business features.

### Recent Updates
-  User registration and authentication
-  JWT-based security implementation
-  Database schema management with Liquibase
-  CQRS pattern implementation
-  Domain-driven design structure

## License

License information is not currently specified. Please contact the project maintainers for licensing details.

---

For more detailed technical information about the architecture, patterns, and development guidelines, please refer to the [CLAUDE.md](.claude/CLAUDE.md) file.