# Test Plan for 10xDevs Cards Backend

## 1. Introduction and Testing Objectives

### Purpose
This test plan defines the testing strategy for the 10xDevs Cards backend application, a Spring Boot-based AI-powered flashcard management system implementing Domain-Driven Design with hexagonal architecture and CQRS pattern.

### Testing Objectives
- Verify security mechanisms, particularly JWT authentication and authorization
- Validate domain model business logic and invariant enforcement
- Ensure AI generation workflow operates correctly with external OpenRouter API
- Confirm data integrity across aggregate boundaries
- Validate resilience patterns (circuit breaker, retry, rate limiting)
- Ensure OpenAPI specification compliance
- Verify performance under expected load conditions

## 2. Scope of Testing

### In Scope
- User authentication and registration
- Flashcard CRUD operations with ownership validation
- AI generation session lifecycle (PENDING → COMPLETED/FAILED)
- Flashcard suggestion approval workflow
- Domain entity business methods and state transitions
- CQRS command/query handler implementations
- Repository layer and database persistence
- MapStruct mapper accuracy
- OpenRouter API integration with resilience patterns
- JWT security filter chain
- Liquibase database migrations
- OpenAPI endpoint contract compliance

### Out of Scope
- Frontend application testing
- Third-party service testing (OpenRouter API internals)
- Infrastructure provisioning (Docker, Kubernetes)
- Performance testing beyond smoke tests
- Security penetration testing (dedicated security audit required)

## 3. Types of Tests

### 3.1 Unit Tests
**Target:** 80%+ code coverage for domain and application layers

**Domain Layer Tests:**
- Domain entity business methods (`complete()`, `fail()`, `ensureOwnedBy()`)
- Invariant validation in constructors and factory methods
- State transition logic (session status changes)
- Snapshot generation accuracy (`toSnapshot()`)
- Value object immutability
- Business query methods (`canProvideSuggestions()`)

**Application Layer Tests:**
- Command handler logic in isolation
- Query handler data retrieval and mapping
- JWT operations (token generation, validation, extraction)
- Service layer business logic

**Infrastructure Layer Tests:**
- MapStruct mapper bidirectional conversion accuracy
- Repository adapter implementations (without database)
- Specification adapters for query building

### 3.2 Integration Tests
**Target:** All critical user flows end-to-end

**Database Integration:**
- Repository save/find operations with real PostgreSQL
- Transaction management and rollback scenarios
- Liquibase migration execution
- Cascade operations for aggregate relationships
- Orphan removal verification

**API Integration:**
- Controller endpoint request/response validation
- Spring Security filter chain execution
- JWT authentication flow (authenticated vs public endpoints)
- Request validation and error responses
- OpenAPI specification compliance
- CORS behavior verification

**External Service Integration:**
- OpenRouter API mocked responses
- Circuit breaker state transitions (CLOSED → OPEN → HALF_OPEN)
- Retry mechanism with backoff verification
- Rate limiting enforcement
- Timeout handling
- Error response mapping

### 3.3 Contract Tests
**Target:** OpenAPI specification alignment

- Request/response schema validation
- HTTP status code verification
- Authentication requirements per endpoint
- Data type and format compliance (UUID, date-time, enums)
- Pagination parameter validation
- Filter and sort parameter behavior

### 3.4 Security Tests
**Target:** OWASP Top 10 vulnerabilities

- Authentication bypass attempts
- Authorization enforcement (user can only access own resources)
- JWT token tampering detection
- Expired token rejection
- SQL injection prevention (via parameterized queries)
- XSS prevention in input validation
- CORS policy enforcement
- Mass assignment protection

### 3.5 Performance Tests (Smoke Level)
**Target:** Basic throughput validation

- Concurrent user registration (50 users)
- Flashcard listing pagination performance (1000+ cards)
- AI generation session creation under load (10 concurrent sessions)
- Database query optimization validation (N+1 query detection)

## 4. Test Scenarios for Key Functionalities

### 4.1 User Authentication & Authorization

| Scenario ID | Description | Priority | Type |
|-------------|-------------|----------|------|
| AUTH-001 | User registers with valid credentials | Critical | Integration |
| AUTH-002 | Registration fails with duplicate username | Critical | Integration |
| AUTH-003 | Registration fails with duplicate email | Critical | Integration |
| AUTH-004 | Registration validates password complexity | High | Unit |
| AUTH-005 | User logs in with correct credentials | Critical | Integration |
| AUTH-006 | Login fails with incorrect password | Critical | Integration |
| AUTH-007 | JWT token contains correct claims | Critical | Unit |
| AUTH-008 | Expired JWT token rejected | Critical | Security |
| AUTH-009 | Tampered JWT token rejected | Critical | Security |
| AUTH-010 | Public endpoints accessible without token | High | Security |
| AUTH-011 | Protected endpoints require valid token | Critical | Security |

### 4.2 Flashcard Management

| Scenario ID | Description | Priority | Type |
|-------------|-------------|----------|------|
| FLASH-001 | Create manual flashcard with valid data | High | Integration |
| FLASH-002 | Creation fails with content exceeding 1000 chars | High | Unit |
| FLASH-003 | Update existing flashcard content | High | Integration |
| FLASH-004 | Update fails for non-existent flashcard | High | Integration |
| FLASH-005 | User cannot update another user's flashcard | Critical | Security |
| FLASH-006 | Delete flashcard permanently | High | Integration |
| FLASH-007 | User cannot delete another user's flashcard | Critical | Security |
| FLASH-008 | List flashcards with pagination (page 0, size 20) | High | Integration |
| FLASH-009 | Filter flashcards by source (AI, AI_USER, USER) | Medium | Integration |
| FLASH-010 | Sort flashcards by createdAt descending | Medium | Integration |
| FLASH-011 | Empty list returned for user with no flashcards | Medium | Integration |

### 4.3 AI Generation Workflow

| Scenario ID | Description | Priority | Type |
|-------------|-------------|----------|------|
| AI-001 | Create AI session with valid input (1000-10000 chars) | Critical | Integration |
| AI-002 | Session creation fails with input < 1000 chars | High | Unit |
| AI-003 | Session created with pre-generated UUID | High | Unit |
| AI-004 | Session status transitions to COMPLETED on success | Critical | Unit |
| AI-005 | Session status transitions to FAILED on API error | Critical | Unit |
| AI-006 | Session saves with correct AI model and cost | High | Integration |
| AI-007 | Generated suggestions stored with session | Critical | Integration |
| AI-008 | Retrieve session status by ID | High | Integration |
| AI-009 | User cannot retrieve another user's session | Critical | Security |
| AI-010 | Get suggestions for COMPLETED session | Critical | Integration |
| AI-011 | Get suggestions fails for PENDING session | High | Unit |
| AI-012 | Get suggestions fails for FAILED session | High | Unit |
| AI-013 | Approve suggestions without edits (source=AI) | Critical | Integration |
| AI-014 | Approve suggestions with edits (source=AI_USER) | Critical | Integration |
| AI-015 | Approval creates flashcards from suggestions | Critical | Integration |
| AI-016 | Approval increments acceptedCount | High | Integration |
| AI-017 | User cannot approve another user's suggestions | Critical | Security |
| AI-018 | Approval fails for non-existent suggestion ID | High | Integration |

### 4.4 Domain Model Business Logic

| Scenario ID | Description | Priority | Type |
|-------------|-------------|----------|------|
| DOMAIN-001 | AiGenerationSession.complete() validates PENDING status | Critical | Unit |
| DOMAIN-002 | AiGenerationSession.fail() sets correct status | Critical | Unit |
| DOMAIN-003 | AiGenerationSession.ensureOwnedBy() throws on mismatch | Critical | Unit |
| DOMAIN-004 | AiGenerationSession.canProvideSuggestions() true for COMPLETED | High | Unit |
| DOMAIN-005 | AiGenerationSession.getSuggestions() returns immutable list | High | Unit |
| DOMAIN-006 | User.newUser() validates email format | High | Unit |
| DOMAIN-007 | User.fromSnapshot() restores state correctly | High | Unit |
| DOMAIN-008 | Flashcard.create() validates content not empty | High | Unit |
| DOMAIN-009 | Snapshot pattern preserves all domain state | Critical | Unit |
| DOMAIN-010 | Factory methods enforce business invariants | Critical | Unit |

### 4.5 Resilience Patterns

| Scenario ID | Description | Priority | Type |
|-------------|-------------|----------|------|
| RESIL-001 | Retry on 5xx server errors from OpenRouter | High | Integration |
| RESIL-002 | Exponential backoff between retries (1s, 2s, 4s) | High | Integration |
| RESIL-003 | Circuit breaker opens after 50% failure rate | High | Integration |
| RESIL-004 | Circuit breaker transitions to half-open after 60s | High | Integration |
| RESIL-005 | Circuit breaker closes after 3 successful calls | High | Integration |
| RESIL-006 | Rate limiting enforced on OpenRouter calls | Medium | Integration |
| RESIL-007 | Timeout after configured duration | Medium | Integration |
| RESIL-008 | Fallback behavior on circuit open | Medium | Integration |

### 4.6 Data Persistence

| Scenario ID | Description | Priority | Type |
|-------------|-------------|----------|------|
| DATA-001 | Aggregate saved atomically (session + suggestions) | Critical | Integration |
| DATA-002 | Orphan suggestions removed with cascade delete | Critical | Integration |
| DATA-003 | Transaction rollback on failure | Critical | Integration |
| DATA-004 | MapStruct mapper converts domain ↔ entity correctly | Critical | Unit |
| DATA-005 | Liquibase migrations execute without errors | High | Integration |
| DATA-006 | Unique constraints enforced (username, email) | High | Integration |
| DATA-007 | Foreign key constraints prevent orphaned data | High | Integration |

## 5. Test Environment

### 5.1 Local Development Environment
- **Java:** OpenJDK 21
- **Build Tool:** Maven 3.9+
- **Database:** PostgreSQL 15+ (Docker container)
- **Docker:** v24.0+ (for PostgreSQL via Docker Compose)
- **IDE:** IntelliJ IDEA / Eclipse with Lombok plugin

### 5.2 Continuous Integration Environment
- **CI/CD Platform:** GitHub Actions / Jenkins
- **Test Database:** Testcontainers with PostgreSQL image
- **Isolation:** Each test run uses fresh database instance
- **Parallelization:** Maven Surefire with 4 parallel threads

### 5.3 Test Data Management
- **Strategy:** Fresh database per test class with Liquibase migrations
- **Fixtures:** Builder pattern for test data creation
- **Cleanup:** Database reset between test classes (Testcontainers restart)
- **Mock Data:** Faker library for realistic user data

## 6. Testing Tools

### 6.1 Unit Testing
- **JUnit 5:** Test framework with parameterized tests
- **AssertJ:** Fluent assertions for readability
- **Mockito:** Mocking framework for dependencies

### 6.2 Integration Testing
- **Spring Boot Test:** `@SpringBootTest` for full context
- **Testcontainers:** PostgreSQL container for database tests
- **WireMock:** OpenRouter API mocking
- **RestAssured:** HTTP endpoint testing with fluent API

### 6.3 Security Testing
- **Spring Security Test:** `@WithMockUser` and security context setup
- **JWT Test Utilities:** Custom token generation for tests

### 6.4 Contract Testing
- **OpenAPI Generator:** Validate generated code matches specification
- **Swagger Parser:** Runtime OpenAPI validation

### 6.5 Performance Testing
- **JMeter:** Load testing for API endpoints
- **Spring Boot Actuator:** Metrics collection during tests

### 6.6 Code Quality
- **JaCoCo:** Code coverage reporting (target: 80%+)
- **SonarQube:** Static code analysis
- **ArchUnit:** Architecture rule enforcement (DDD patterns)

## 7. Test Schedule

### Phase 1: Unit Tests (Weeks 1-2)
- Domain entity business logic
- Command/query handlers
- MapStruct mappers
- JWT operations

**Deliverable:** 80% coverage for domain + application layers

### Phase 2: Integration Tests (Weeks 3-4)
- Database persistence tests
- API endpoint tests with authentication
- OpenRouter mocked integration
- Liquibase migration validation

**Deliverable:** All critical user flows covered

### Phase 3: Security & Resilience Tests (Week 5)
- Authorization enforcement
- JWT security scenarios
- Circuit breaker and retry tests
- Rate limiting validation

**Deliverable:** Security test suite passing

### Phase 4: Contract & Performance Tests (Week 6)
- OpenAPI compliance validation
- Smoke performance tests
- Concurrency testing

**Deliverable:** Performance baseline established

### Ongoing: Regression Testing
- Execute full suite on every commit (CI pipeline)
- Nightly extended test suite with performance tests

## 8. Test Acceptance Criteria

### Unit Tests
- ✅ 80%+ line coverage for domain and application layers
- ✅ All domain business methods tested with edge cases
- ✅ All command/query handlers tested in isolation
- ✅ Zero test failures in CI pipeline

### Integration Tests
- ✅ All user flows (registration, login, flashcard CRUD, AI workflow) covered
- ✅ Database transactions validated with rollback scenarios
- ✅ OpenRouter API integration tested with all error cases
- ✅ Liquibase migrations execute cleanly on test database

### Security Tests
- ✅ Authentication bypass attempts fail
- ✅ Authorization enforced (users cannot access others' resources)
- ✅ JWT token tampering detected
- ✅ OWASP Top 10 vulnerabilities mitigated

### Performance Tests
- ✅ 50 concurrent users can register without errors
- ✅ Flashcard listing responds within 500ms (1000 cards)
- ✅ AI session creation handles 10 concurrent requests
- ✅ No N+1 query issues detected

### Code Quality
- ✅ JaCoCo reports 80%+ coverage
- ✅ SonarQube quality gate passes (no critical/blocker issues)
- ✅ ArchUnit rules enforce DDD patterns (no getter violations)

## 9. Roles and Responsibilities

| Role | Responsibilities | Personnel |
|------|-----------------|-----------|
| **QA Lead** | Test plan creation, test strategy oversight, reporting | [Name] |
| **Backend Developers** | Unit test implementation, test fixture creation | Development Team |
| **QA Engineers** | Integration test implementation, manual exploratory testing | QA Team |
| **Security Engineer** | Security test design, vulnerability assessment | Security Team |
| **DevOps Engineer** | CI/CD pipeline configuration, Testcontainers setup | DevOps Team |
| **Product Owner** | Acceptance criteria validation, priority definition | [Name] |

### Collaboration Model
- **Daily:** Developers write unit tests alongside feature code (TDD encouraged)
- **Weekly:** QA review of test coverage and integration test status
- **Sprint End:** Full regression suite execution and results review
- **Monthly:** Security test review and vulnerability assessment

## 10. Bug Reporting Procedures

### Bug Lifecycle
1. **Discovery:** Test failure or defect identified
2. **Triage:** Severity and priority assigned
3. **Assignment:** Ticket assigned to responsible developer
4. **Fix:** Developer implements fix with regression test
5. **Verification:** QA verifies fix in test environment
6. **Closure:** Bug closed after production deployment

### Severity Levels

**Critical (P0):**
- Security vulnerabilities (authentication bypass, data exposure)
- Data corruption or loss
- System crashes or unavailability
- **SLA:** Fix within 24 hours

**High (P1):**
- Major functional failures (AI generation broken, flashcard creation fails)
- Performance degradation (> 2x expected response time)
- **SLA:** Fix within 3 business days

**Medium (P2):**
- Minor functional issues (incorrect validation message)
- UI/UX inconsistencies
- **SLA:** Fix within 1 sprint

**Low (P3):**
- Cosmetic issues, minor typos
- Enhancement requests
- **SLA:** Backlog prioritization

### Bug Report Template
```markdown
**Title:** [Concise description]

**Severity:** [Critical/High/Medium/Low]

**Environment:** [Local/CI/Staging]

**Steps to Reproduce:**
1. [Step 1]
2. [Step 2]
3. [Step 3]

**Expected Result:** [What should happen]

**Actual Result:** [What actually happened]

**Test Scenario ID:** [Reference to test plan, e.g., AI-005]

**Logs/Screenshots:** [Attach relevant evidence]

**Regression Test:** [Link to test case preventing recurrence]
```

### Reporting Tools
- **Issue Tracker:** Jira with custom workflow
- **Test Management:** TestRail for test case tracking
- **CI Integration:** Automated bug creation on test failure (Critical/High only)
- **Notifications:** Slack channel #qa-alerts for P0/P1 issues

---

**Document Version:** 1.0
**Date:** 2025-11-12
**Status:** Draft for Review
**Next Review:** Sprint Planning Meeting