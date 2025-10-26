# 10xDevs Cards Backend

A Spring Boot 3.5.7 backend application for the 10xDevs Cards project using Java 21, PostgreSQL, and JWT authentication.

## Quick Start

### Prerequisites

- Java 21
- Docker and Docker Compose
- Maven (or use the included Maven wrapper)

### Running the Application

1. **Start the PostgreSQL database:**
   ```bash
   docker compose up postgres
   ```

2. **Run the Spring Boot application:**
   ```bash
   ./mvnw spring-boot:run
   ```

The application will start on `http://localhost:8080`.

### Database Setup

The application uses PostgreSQL running in Docker. The database will be automatically created with the following configuration:

- **Database:** `tenx_cards`
- **Username:** `postgres`
- **Password:** `password`
- **Port:** `5432`

Database schema is managed by Liquibase and will be automatically applied on startup.

### API Endpoints

- `POST /users/register` - User registration
- `POST /auth/login` - User login
- `GET /users` - Get users (requires authentication)

### Testing

Run all tests:
```bash
./mvnw test
```

### Environment Variables

Optional environment variables:
- `DB_USERNAME` - Database username (default: `postgres`)
- `DB_PASSWORD` - Database password (default: `password`)

## Architecture

This project follows Domain-Driven Design (DDD) with hexagonal architecture and CQRS patterns. See [CLAUDE.md](CLAUDE.md) for detailed architectural documentation.