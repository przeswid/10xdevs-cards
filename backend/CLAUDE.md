# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.7 backend application for the 10xDevs Cards project. It uses Java 21, PostgreSQL database, Spring Security with JWT authentication, and follows Domain-Driven Design (DDD) with hexagonal architecture principles.

## Build and Development Commands

### Building and Running
- `./mvnw clean compile` - Compile the project
- `./mvnw spring-boot:run` - Run the application (starts on default port 8080)
- `./mvnw clean package` - Build JAR file
- `./mvnw clean install` - Full build with tests

### Testing
- `./mvnw test` - Run all tests
- `./mvnw test -Dtest=CardsApplicationTests` - Run specific test class
- `./mvnw test -Dtest=CardsApplicationTests#contextLoads` - Run specific test method

### Database Setup
The application requires a PostgreSQL database. Use Docker Compose for local development:
- `docker compose up postgres` - Start PostgreSQL container
- Database: `tenx_cards`
- Default connection: `jdbc:postgresql://localhost:5432/tenx_cards`
- Username: `postgres` (configurable via `DB_USERNAME` env var)
- Password: `password` (configurable via `DB_PASSWORD` env var)

## Architecture and Package Structure

### Domain-Driven Design Structure
The project follows DDD with hexagonal architecture:

```
com.ten.devs.cards.cards/
├── CardsApplication.java
├── ApplicationConfiguration.java
├── SecurityConfiguration.java
├── sharedkernel/
│   └── authentication/          # JWT authentication shared kernel
│       ├── AuthenticationController.java
│       ├── AuthenticationService.java
│       ├── JwtService.java
│       ├── JwtAuthenticationFilter.java
│       └── dto/
└── user/                        # User bounded context
    ├── domain/                  # Core business logic
    │   ├── User.java           # Domain entity (immutable)
    │   ├── UserId.java         # Value object
    │   ├── UserSnapshot.java   # Data transfer object
    │   ├── Role.java           # Enum
    │   └── UserRepository.java # Repository interface
    ├── application/             # Use cases/services
    │   ├── UserService.java
    │   └── dto/
    ├── infrastructure/          # Technical implementation
    │   ├── UserEntity.java     # JPA entity
    │   ├── UserJpaRepository.java
    │   ├── SqlDbUserRepository.java
    │   └── UserMapper.java     # MapStruct mapper
    └── presentation/            # Web layer
        ├── UserController.java
        └── UserResponse.java
```

### Key Architectural Patterns

**Domain Layer:**
- Immutable domain entities with factory methods (`User.newUser()`, `User.fromSnapshot()`)
- Value objects for strong typing (`UserId`)
- Repository interfaces define contracts
- Domain entities are separate from JPA entities

**Infrastructure Layer:**
- JPA entities (`UserEntity`) implement Spring Security `UserDetails`
- MapStruct mappers handle domain ↔ infrastructure conversion
- Repository implementations bridge domain and JPA repositories
- Uses Liquibase for database schema management (`ddl-auto: none`)

**Security Configuration:**
- JWT authentication with custom filter chain
- Stateless session management
- CORS configured for `http://localhost:8005`
- Public endpoints: `/users/register`, `/auth/login`
- JWT secret and expiration configured in YAML

**Data Persistence:**
- User table name: `users` (not `userEntities`)
- User roles stored in separate `user_roles` table via `@ElementCollection`
- No automatic timestamp management (handled by domain if needed)
- Unique constraints on `username` and `email`

## Configuration Details

**Database:**
- PostgreSQL with Hibernate dialect
- Liquibase for schema management
- Change logs in `src/main/resources/db/changelog/`
- SQL formatting enabled, logging disabled

**Dependencies:**
- Spring Boot 3.5.7 (Web, Security, JPA, Actuator, Validation)
- PostgreSQL driver + Liquibase
- JWT processing (JJWT v0.11.5)
- Lombok + MapStruct for code generation
- Jakarta validation

## Development Notes

- The project strictly separates domain logic from infrastructure concerns
- Domain entities are immutable and use snapshots for state transfer
- MapStruct generates type-safe mappings between layers
- JWT authentication is fully implemented with proper security configuration
- Use the provided Docker Compose for consistent database setup