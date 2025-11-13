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

The 10xDevs Cards Backend is a robust Spring Boot 3.5.7 API that enables users to quickly create and manage educational flashcard sets. The application leverages LLM models via API to generate intelligent flashcard suggestions based on provided text, significantly reducing the time and effort required for manual flashcard creation.

Built with modern architectural patterns including Domain-Driven Design (DDD), hexagonal architecture, and Command Query Responsibility Segregation (CQRS), the backend ensures maintainability, scalability, and clear separation of concerns while providing comprehensive user management, authentication services, and AI-powered flashcard generation capabilities.

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
- **LLM Integration** - AI-powered flashcard generation via external APIs

### Development Tools
- **Spring Boot Actuator** - Application monitoring
- **Spring Boot Validation** - Input validation
- **Spring Security Test** - Security testing utilities

### Testing Frameworks & Libraries
- **JUnit 5** - Primary test framework with parameterized test support
- **AssertJ** - Fluent assertions for improved test readability
- **Mockito** - Mocking framework for unit test dependencies
- **Spring Boot Test** - `@SpringBootTest` for full application context testing
- **Testcontainers** - PostgreSQL container for integration tests
- **WireMock** - HTTP mocking for external API (OpenRouter) testing
- **RestAssured** - Fluent API for HTTP endpoint testing
- **JaCoCo** - Code coverage reporting (target: 80%+)
- **SonarQube** - Static code analysis and quality gates
- **ArchUnit** - Architecture rule enforcement for DDD patterns

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
- **Protected endpoints:**
  - `GET /flashcards` - Get user's flashcards
  - `POST /flashcards` - Create flashcard manually
  - `POST /flashcards/generate` - Generate flashcards using AI
  - `PUT /flashcards/{id}` - Update flashcard
  - `DELETE /flashcards/{id}` - Delete flashcard
  - `POST /study-sessions` - Start study session
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

### Core Features

#### 1. AI-Powered Flashcard Generation
- **Text Input Processing:** Users can paste text (1,000-10,000 characters) from textbooks, articles, or study materials
- **LLM Integration:** Automatic communication with external LLM APIs to generate contextual flashcard suggestions
- **Intelligent Suggestions:** AI creates question-answer pairs based on the provided content
- **User Review System:** Users can accept, edit, or reject generated flashcards before saving

#### 2. Manual Flashcard Management
- **Custom Creation:** Manual flashcard creation with front/back content definition
- **Full CRUD Operations:** Create, read, update, and delete flashcards
- **Personal Library:** "My Flashcards" view for managing all user flashcards
- **Batch Operations:** Efficient handling of multiple flashcard operations

#### 3. User Authentication & Security
- **Secure Registration/Login:** Email and password-based authentication
- **JWT Token Management:** Stateless authentication with secure token handling
- **Data Privacy:** User flashcards are completely private and isolated
- **Account Management:** Users can delete their account and all associated data
- **GDPR Compliance:** Full compliance with data protection regulations

#### 4. Spaced Repetition Integration
- **Study Sessions:** Organized learning sessions based on spaced repetition algorithms
- **Progress Tracking:** User performance assessment and learning progress monitoring
- **Algorithm Integration:** Uses proven open-source spaced repetition libraries
- **Adaptive Learning:** Flashcard scheduling based on user performance

#### 5. Analytics & Insights
- **Generation Statistics:** Tracking of AI-generated vs. manually created flashcards
- **Acceptance Rates:** Monitoring how many AI suggestions are accepted by users
- **Usage Metrics:** Understanding user engagement and learning patterns

### Architecture Highlights
- **Domain-Driven Design:** Clear bounded contexts separating User, Flashcard, and Study domains
- **Hexagonal Architecture:** Clean separation of business logic from infrastructure concerns
- **CQRS Pattern:** Commands for write operations, queries for read operations
- **Immutable Domain Entities:** Domain entities with factory methods and snapshots
- **Type-Safe Mapping:** MapStruct-generated mappings between architectural layers
- **Event-Driven Architecture:** Domain events for cross-context communication

### MVP Scope (Current Development)
- ✅ User registration and authentication
- ✅ JWT-based security implementation
- ✅ Database schema management with Liquibase
- ✅ CQRS pattern implementation
- ✅ Domain-driven design structure
- 🚧 Flashcard CRUD operations
- 🚧 AI integration for flashcard generation
- 🚧 Spaced repetition algorithm integration
- 🚧 Study session management

### Out of Scope for MVP
- Advanced proprietary spaced repetition algorithms
- Gamification features
- Mobile applications (web-only initially)
- Document import (PDF, DOCX, etc.)
- Public API access
- Flashcard sharing between users
- Advanced notification systems
- Keyword-based flashcard search

## Project Status

🚧 **Development Phase** - Version 0.0.1-SNAPSHOT

The project is currently in active development with core authentication and user management features implemented. The architectural foundation is solid and ready for expansion with AI-powered flashcard generation and spaced repetition features.

### Success Metrics (Target)
- **AI Efficiency:** 75% of AI-generated flashcards should be accepted by users
- **AI Adoption:** 75% of all flashcards should be created using AI assistance
- **User Engagement:** Active monitoring of generation vs. approval rates for quality analysis

### Development Progress
- ✅ User registration and authentication system
- ✅ JWT-based security implementation
- ✅ Database schema management with Liquibase
- ✅ CQRS pattern implementation
- ✅ Domain-driven design structure
- 🚧 Flashcard domain implementation
- 🚧 AI integration layer for LLM communication
- 🚧 Spaced repetition algorithm integration
- 🚧 Study session management
- 📋 Analytics and metrics collection

### Next Milestones
1. **Flashcard Management** - Complete CRUD operations for flashcards
2. **AI Integration** - Implement LLM API communication for flashcard generation
3. **Study Sessions** - Integrate spaced repetition algorithm
4. **Analytics** - Implement usage tracking and success metrics
5. **Frontend Integration** - API endpoints ready for frontend consumption

## License

License information is not currently specified. Please contact the project maintainers for licensing details.

---

For more detailed technical information about the architecture, patterns, and development guidelines, please refer to the [CLAUDE.md](.claude/CLAUDE.md) file.