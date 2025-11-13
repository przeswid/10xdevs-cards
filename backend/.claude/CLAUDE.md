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
The project follows DDD with hexagonal architecture and CQRS pattern using Pipelinr library:

```
com.ten.devs.cards.cards/
├── CardsApplication.java
├── config/
│   ├── ApplicationConfiguration.java
│   ├── WebConfig.java
│   └── auth/
│       ├── SecurityConfiguration.java
│       └── JwtAuthenticationFilter.java
└── user/                        # User bounded context
    ├── domain/                  # Core business logic
    │   ├── User.java           # Domain entity (MUTABLE, no getters)
    │   ├── UserId.java         # Value object (immutable)
    │   ├── UserSnapshot.java   # Snapshot DTO (immutable)
    │   ├── Role.java           # Enum
    │   ├── UserRepository.java # Repository interface
    │   └── UserSpecification.java # Domain specifications
    ├── application/             # Use cases/commands/queries
    │   ├── command/            # Command handlers (CQRS)
    │   │   ├── RegisterUserCommand.java
    │   │   ├── RegisterUserCommandHandler.java
    │   │   ├── LoginUserCommand.java
    │   │   ├── LoginUserCommandHandler.java
    │   │   └── JwtOperations.java
    │   └── query/              # Query handlers (CQRS)
    │       ├── GetUsersQuery.java
    │       └── GetUsersQueryHandler.java
    ├── infrastructure/          # Technical implementation
    │   └── db/
    │       ├── UserEntity.java     # JPA entity
    │       ├── UserJpaRepository.java
    │       ├── SqlDbUserRepository.java
    │       ├── UserMapper.java     # MapStruct mapper
    │       └── UserSpecificationAdapter.java
    └── presentation/            # Web layer
        ├── AuthenticationController.java
        ├── UserController.java
        ├── request/
        │   ├── RegisterUserRequest.java
        │   └── LoginRequest.java
        └── response/
            ├── GetUserResponse.java
            └── LoginResponse.java
```

### Key Architectural Patterns

**CQRS (Command Query Responsibility Segregation):**
- Uses Pipelinr library for implementing CQRS pattern
- Commands for write operations (`RegisterUserCommand`, `LoginUserCommand`)
- Queries for read operations (`GetUsersQuery`)
- Command/Query handlers are separate components with single responsibility
- Commands return domain identifiers, queries return DTOs

**Domain Layer:**
- **MUTABLE domain entities** with factory methods (`User.newUser()`, `User.fromSnapshot()`)
- **NO GETTERS** - domain entities should NOT expose getters (see DDD Best Practices below)
- Business methods modify entity state directly (e.g., `session.complete()`, `session.fail()`)
- Value objects for strong typing (`UserId`) - these CAN be immutable records
- Repository interfaces define contracts (PORTS in hexagonal architecture)
- Domain entities are separate from JPA entities
- Domain specifications for complex queries (`UserSpecification`)

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
- Pipelinr v0.11 for CQRS implementation
- Lombok + MapStruct for code generation
- Jakarta validation

**Testing Dependencies:**
- JUnit 5 - Test framework with parameterized test support
- AssertJ - Fluent assertions for test readability
- Mockito - Mocking framework for unit tests
- Spring Boot Test - Full application context testing
- Testcontainers - PostgreSQL container for integration tests
- WireMock - External API mocking (OpenRouter)
- RestAssured - HTTP endpoint testing with fluent API
- JaCoCo - Code coverage reporting (target: 80%+)
- SonarQube - Static code analysis and quality gates
- ArchUnit - Architecture rule enforcement for DDD patterns

## DDD Best Practices - CRITICAL RULES

This section documents critical DDD patterns that MUST be followed to avoid the **Anemic Domain Model anti-pattern**.

### 1. Domain Entities Are MUTABLE (Vaughn Vernon's recommendation)

**✅ CORRECT:**
```java
public class AiGenerationSession {
    private AiGenerationSessionStatus status;  // Mutable field

    public void complete(int generatedCount, String aiModel, BigDecimal apiCost) {
        if (status != AiGenerationSessionStatus.PENDING) {
            throw new IllegalStateException("Can only complete PENDING sessions");
        }
        this.generatedCount = generatedCount;  // Modify state
        this.status = AiGenerationSessionStatus.COMPLETED;
    }
}
```

**❌ WRONG:**
```java
public record AiGenerationSession(...) {  // Immutable record
    public AiGenerationSession complete(...) {
        return new AiGenerationSession(...);  // Creating new instances
    }
}
```

**Why:** Domain entities have lifecycles and state transitions. Making them immutable forces you to create new instances for every state change, which is unnatural and complicates persistence.

### 2. NO GETTERS in Domain Entities - Use Business Methods Instead

**This is THE MOST IMPORTANT RULE to avoid Anemic Domain Model.**

**✅ CORRECT - Business logic INSIDE domain:**
```java
public class AiGenerationSession {
    // Private fields - NO public getters!
    private UUID userId;
    private AiGenerationSessionStatus status;

    // Business method encapsulates ownership check
    public void ensureOwnedBy(UUID userId) {
        if (!this.userId.equals(userId)) {
            throw new IllegalArgumentException("Session not owned by user");
        }
    }

    // Business query method
    public boolean canProvideSuggestions() {
        return status == AiGenerationSessionStatus.COMPLETED;
    }

    // toSnapshot() is the ONLY way to access state externally
    public AiGenerationSessionSnapshot toSnapshot() {
        return AiGenerationSessionSnapshot.builder()
            .id(id.value())
            .userId(userId)
            .status(status)
            .build();
    }
}
```

**❌ WRONG - Business logic OUTSIDE domain (Anemic Domain Model):**
```java
public class AiGenerationSession {
    public UUID getUserId() { return userId; }  // ❌ GETTER!
    public AiGenerationSessionStatus getStatus() { return status; }  // ❌ GETTER!
}

// Handler has business logic - THIS IS THE ANTI-PATTERN!
class GetAiSuggestionsCommandHandler {
    public GetAiSuggestionsResponse handle(GetAiSuggestionsCommand command) {
        AiGenerationSession session = repository.findById(command.sessionId());

        // ❌ Business logic outside domain!
        if (!session.getUserId().equals(command.userId())) {
            throw new IllegalArgumentException("Not owned by user");
        }

        // ❌ Business rule outside domain!
        if (session.getStatus() != AiGenerationSessionStatus.COMPLETED) {
            return new GetAiSuggestionsResponse(/*...*/);
        }
    }
}
```

### 3. The Snapshot Pattern - Crossing Architectural Boundaries

**Purpose:** Snapshots are immutable DTOs that transfer domain state across architectural boundaries (domain ↔ infrastructure, domain ↔ presentation).

**✅ CORRECT usage:**
```java
// In command handler
AiGenerationSession session = AiGenerationSession.create(userId, inputText);
session.complete(count, model, cost);  // Business method modifies state

// Save to database - mapper uses toSnapshot()
session = sessionRepository.save(session);

// Create response DTO - use toSnapshot()
AiGenerationSessionSnapshot snapshot = session.toSnapshot();
return new CreateAiSessionResponse(
    snapshot.id(),
    snapshot.status().name(),
    snapshot.createdAt()
);
```

**Key Rules:**
- Domain entities expose `toSnapshot()` method - this is the ONLY way to access state externally
- Snapshots are immutable (records or classes with final fields)
- MapStruct mappers convert: `toSnapshot()` → JPA entity → `toSnapshot()` → domain entity
- Handlers use snapshots to create response DTOs
- Never use getters on domain entities - always use `toSnapshot()` or business methods

### 4. Minimize Database Saves - Save Once with Final State

**✅ CORRECT - Save ONCE:**
```java
// Create in memory
AiGenerationSession session = AiGenerationSession.create(userId, inputText);
AiGenerationSessionSnapshot initialSnapshot = session.toSnapshot();

try {
    // Generate flashcards
    List<FlashcardSuggestion> suggestions = aiService.generateFlashcards(...);

    // Mark as completed (in memory)
    session.complete(suggestions.size(), model, cost);

    // Save ONCE with COMPLETED status
    session = sessionRepository.save(session);
} catch (Exception e) {
    // Mark as failed (in memory)
    session.fail();

    // Save ONCE with FAILED status
    session = sessionRepository.save(session);
}
```

**❌ WRONG - Multiple saves:**
```java
// Save as PENDING
session = sessionRepository.save(session);  // ❌ Unnecessary save!

// Generate flashcards
session.complete(...);

// Save as COMPLETED
session = sessionRepository.save(session);  // ❌ Second save!
```

### 5. Rich Domain Model Checklist

When creating or reviewing domain entities, verify:

- ✅ Entity has business methods that encapsulate behavior (`complete()`, `fail()`, `ensureOwnedBy()`)
- ✅ Entity has NO public getters
- ✅ Entity validates invariants in business methods and constructors
- ✅ Entity exposes `toSnapshot()` for external state access
- ✅ Command/Query handlers use business methods, NOT getters
- ✅ Handlers use `toSnapshot()` for creating response DTOs
- ✅ Business rules are INSIDE domain entities, NOT in handlers/services

### 6. Value Objects vs Entities

**Value Objects** (e.g., `UserId`, `FlashcardSuggestion`):
- CAN be immutable records
- Defined by their attributes (no identity)
- Can have accessor methods (records generate them automatically)
- Example: `public record UserId(UUID value) {}`

**Domain Entities** (e.g., `AiGenerationSession`, `User`):
- MUTABLE with business methods
- Have identity (ID field)
- NO getters - use business methods and `toSnapshot()`
- Example: `public class AiGenerationSession { /* business methods */ }`

### 7. Aggregate Boundaries - Single Aggregate Pattern

**Critical Decision:** Determine if related entities should be separate aggregates or part of one aggregate.

**✅ CORRECT - Single Aggregate (FlashcardSuggestion is part of AiGenerationSession):**
```java
public class AiGenerationSession {
    private final AiGenerationSessionId id;
    private List<FlashcardSuggestion> suggestions;  // Part of aggregate

    public void complete(List<FlashcardSuggestion> suggestions, String model, BigDecimal cost) {
        this.suggestions = new ArrayList<>(suggestions);  // Defensive copy
        this.generatedCount = suggestions.size();
        this.status = COMPLETED;
    }

    // Controlled access to child entities
    public List<FlashcardSuggestion> getSuggestions() {
        return Collections.unmodifiableList(suggestions);
    }
}

// Infrastructure mapping
@Entity
class AiGenerationSessionEntity {
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "session_id")
    private List<FlashcardSuggestionEntity> suggestions;
}

// Single repository - no separate SuggestionRepository needed
interface AiGenerationSessionRepository {
    AiGenerationSession save(AiGenerationSession session);  // Saves entire aggregate
}
```

**❌ WRONG - Separate Aggregates:**
```java
// Separate repositories - unnecessary complexity
interface AiGenerationSessionRepository { }
interface FlashcardSuggestionRepository { }  // ❌ NOT NEEDED!

// Handler manages two repositories
class Handler {
    void handle(Command cmd) {
        session = sessionRepo.save(session);
        suggestionRepo.saveAll(suggestions);  // ❌ Separate save!
    }
}
```

**Decision Criteria - Use Single Aggregate When:**
- ✅ Child entities have NO independent lifecycle (suggestions deleted with session)
- ✅ Child entities ALWAYS accessed via parent (never query "all suggestions across sessions")
- ✅ Strong consistency needed (session COMPLETED implies suggestions exist)
- ✅ Child entities are conceptually "part of" the parent
- ✅ Performance is acceptable (5-10 suggestions per session)

**Decision Criteria - Use Separate Aggregates When:**
- ❌ Child entities have independent lifecycle
- ❌ Child entities queried without parent context
- ❌ Different consistency boundaries needed
- ❌ Performance requires separate loading (large collections)

**Benefits of Single Aggregate:**
- Atomic persistence (one `save()` call for entire aggregate)
- Enforced consistency rules
- Simplified API (one repository instead of two)
- No orphaned data (JPA `orphanRemoval = true`)
- Eventual consistency within aggregate is acceptable

### 8. Pre-generated ID Pattern - Create Domain Object with Final State

**Pattern:** Generate entity ID upfront, create domain object once with its final state instead of creating temporary PENDING object.

**✅ CORRECT - Pre-generated ID:**
```java
class CreateAiGenerationSessionCommandHandler {
    public Response handle(Command cmd) {
        // 1. Generate ID upfront
        UUID sessionId = UUID.randomUUID();

        AiGenerationSession session;

        try {
            // 2. Call external service with pre-generated ID
            List<FlashcardSuggestion> suggestions = aiService.generateFlashcards(
                cmd.inputText(),
                sessionId
            );

            // 3. Create domain object with COMPLETED state
            session = AiGenerationSession.createCompleted(
                sessionId,
                cmd.userId(),
                cmd.inputText(),
                suggestions,
                model,
                cost
            );

            // 4. Save ONCE with final state
            session = repository.save(session);

        } catch (Exception e) {
            // Create with FAILED state
            session = AiGenerationSession.createFailed(sessionId, userId, inputText);
            session = repository.save(session);
        }
    }
}
```

**❌ WRONG - Temporary PENDING Object:**
```java
class Handler {
    public Response handle(Command cmd) {
        // 1. Create PENDING session just to get ID
        AiGenerationSession session = AiGenerationSession.create(userId, text);
        AiGenerationSessionSnapshot snapshot = session.toSnapshot();  // ❌ Extra step!
        UUID sessionId = snapshot.id();

        // 2. Call service
        List<Suggestion> suggestions = aiService.generate(text, sessionId);

        // 3. Modify state
        session.complete(suggestions, model, cost);  // ❌ State transition

        // 4. Save
        session = repository.save(session);
    }
}
```

**Factory Methods for Different States:**
```java
public class AiGenerationSession {

    // Creates COMPLETED session with pre-generated ID
    public static AiGenerationSession createCompleted(
            UUID sessionId,
            UUID userId,
            String inputText,
            List<FlashcardSuggestion> suggestions,
            String aiModel,
            BigDecimal apiCost) {

        validateInputText(inputText);
        if (suggestions == null || suggestions.isEmpty()) {
            throw new IllegalArgumentException("Cannot create completed session without suggestions");
        }

        return new AiGenerationSession(
            AiGenerationSessionId.of(sessionId),  // Use provided ID
            userId,
            inputText,
            suggestions,
            suggestions.size(),
            0,
            aiModel,
            apiCost,
            AiGenerationSessionStatus.COMPLETED,  // Final state
            Instant.now()
        );
    }

    // Creates FAILED session with pre-generated ID
    public static AiGenerationSession createFailed(
            UUID sessionId,
            UUID userId,
            String inputText) {

        validateInputText(inputText);

        return new AiGenerationSession(
            AiGenerationSessionId.of(sessionId),
            userId,
            inputText,
            new ArrayList<>(),
            0,
            0,
            null,
            null,
            AiGenerationSessionStatus.FAILED,  // Final state
            Instant.now()
        );
    }

    // Still available for cases where state transitions are needed
    public static AiGenerationSession create(UUID userId, String inputText) {
        // Creates PENDING session with auto-generated ID
    }
}
```

**Benefits:**
- No temporary objects in memory
- Domain object created once with final state
- Clearer intent via named factory methods (`createCompleted` vs `create` + `complete`)
- Simpler handler code (no snapshot extraction for ID)
- Same persistence model (still save once)

**When to Use:**
- ✅ External service needs ID before domain object can be created
- ✅ Object state known at creation time
- ✅ Want to avoid intermediate state transitions

**When NOT to Use:**
- ❌ State determined by business logic after creation
- ❌ Multiple state transitions needed
- ❌ ID must be generated by domain logic

### 9. Exception to "NO GETTERS" Rule - Accessing Child Entities in Aggregates

**When you have child entities within an aggregate (like `suggestions` in `AiGenerationSession`), you MAY expose a getter, BUT:**

1. **Return immutable collections** to prevent external modification
2. **Name it as a business query** (e.g., `getSuggestions()` is acceptable)
3. **Document why it's needed** (accessing child entities)

**✅ CORRECT - Controlled access to child entities:**
```java
public class AiGenerationSession {
    private List<FlashcardSuggestion> suggestions;

    /**
     * Business query - returns immutable copy of suggestions.
     * Provides controlled access to child entities without exposing internal state.
     */
    public List<FlashcardSuggestion> getSuggestions() {
        return Collections.unmodifiableList(suggestions);  // ✅ Immutable
    }
}

// Handler uses it to access child entities
class GetAiSuggestionsCommandHandler {
    public Response handle(Command cmd) {
        AiGenerationSession session = repository.findById(cmd.sessionId());

        List<FlashcardSuggestion> suggestions = session.getSuggestions();  // ✅ OK

        return mapToResponse(suggestions);
    }
}
```

**❌ WRONG - Mutable access:**
```java
public List<FlashcardSuggestion> getSuggestions() {
    return suggestions;  // ❌ Allows external modification!
}
```

**Key Distinction:**
- **Scalar getters (NO):** `getUserId()`, `getStatus()` - business logic should use these internally
- **Child entity getters (YES, with restrictions):** `getSuggestions()` - child entities need external access
- **Alternative:** Could use `toSnapshot()` which includes suggestions, but `getSuggestions()` is more explicit

### 10. Examples from This Codebase

**Domain Entity with Business Methods:**
- `AiGenerationSession.java` - Perfect example of rich domain model
  - Business methods: `complete()`, `fail()`, `ensureOwnedBy()`, `canProvideSuggestions()`
  - NO scalar getters
  - Has `getSuggestions()` for child entity access (immutable)
  - Uses `toSnapshot()` pattern
  - Factory methods: `create()`, `createCompleted()`, `createFailed()`

**Single Aggregate Pattern:**
- `AiGenerationSession` contains `List<FlashcardSuggestion>`
- No separate `FlashcardSuggestionRepository`
- Infrastructure uses `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)`
- Single `save()` operation persists entire aggregate

**Pre-generated ID Pattern:**
- `CreateAiGenerationSessionCommandHandler.java` - Generates UUID upfront
  - Calls AI service with pre-generated session ID
  - Uses `createCompleted()` or `createFailed()` factory methods
  - Creates domain object once with final state

**Command Handlers Using Business Methods:**
- `CreateAiGenerationSessionCommandHandler.java` - Uses `createCompleted()`, `createFailed()`
- `GetAiSuggestionsCommandHandler.java` - Uses `session.ensureOwnedBy()`, `session.canProvideSuggestions()`, `session.getSuggestions()`

**MapStruct Mapper Using Snapshots:**
- `AiGenerationSessionMapper.java` - Uses `domain.toSnapshot()`, includes `FlashcardSuggestionMapper`
- Mapper declaration: `@Mapper(componentModel = "spring", uses = FlashcardSuggestionMapper.class)`

## Development Notes

- The project strictly separates domain logic from infrastructure concerns
- **Domain entities are MUTABLE** and use snapshots for state transfer across boundaries
- MapStruct generates type-safe mappings between layers
- JWT authentication is fully implemented with proper security configuration
- Use the provided Docker Compose for consistent database setup