
# Test Plan: 10xDevs Cards Backend (v0.0.1)

---

## 1. Introduction and Testing Objectives

### 1.1. Introduction

This document outlines the comprehensive testing strategy for the **10xDevs Cards Backend**, a Spring Boot application designed for creating and managing educational flashcards. The application features a modern architecture using Domain-Driven Design (DDD), Hexagonal Architecture, and CQRS, with a core focus on AI-powered flashcard generation via an external LLM service (OpenRouter).

This test plan details the scope, methods, resources, and schedule for all testing activities to ensure the application is reliable, secure, scalable, and meets all functional requirements as defined in the project documentation.

### 1.2. Testing Objectives

The primary objectives of this testing phase are to:

-   **Verify Core Functionality:** Ensure that all features, including user management, manual flashcard CRUD, and the complete AI generation workflow, function as expected.
-   **Guarantee Security and Data Integrity:** Validate the robustness of the authentication system (JWT), authorization mechanisms, and critically, the data isolation enforced by PostgreSQL's Row-Level Security (RLS) policies.
-   **Confirm Architectural Soundness:** Validate the correct implementation of DDD, CQRS, and Hexagonal architecture patterns, ensuring separation of concerns and maintainability.
-   **Assess External API Integration:** Test the resilience and correctness of the integration with the OpenRouter LLM service, including error handling, retries, and response parsing.
-   **Ensure GDPR Compliance:** Verify that user data is handled correctly, particularly the "Right to be Forgotten" feature implemented via cascading deletes in the database.
-   **Identify Performance Bottlenecks:** Conduct basic performance and load testing on critical endpoints to ensure responsiveness under expected load.
-   **Validate API Contract:** Ensure the API adheres to the specifications outlined in the `open-api.yaml` document.

---

## 2. Scope of Testing

### 2.1. In-Scope Features

-   **User Management & Authentication:**
    -   User Registration (`POST /auth/register`)
    -   User Login & JWT Generation (`POST /auth/login`)
    -   JWT validation and endpoint protection for all secured routes.
-   **Manual Flashcard Management:**
    -   Creating, Reading, Updating, and Deleting flashcards (`/flashcards` endpoints).
    -   Pagination, sorting, and filtering of flashcard lists.
-   **AI-Powered Flashcard Generation Workflow:**
    -   Creation and status tracking of AI generation sessions (`/ai/sessions`).
    -   Retrieval of AI-generated suggestions.
    -   Approval of suggestions (with and without edits) and their conversion into flashcards.
-   **Database & Data Integrity:**
    -   Schema correctness via Liquibase migrations.
    -   Foreign key constraints (CASCADE DELETE for GDPR, SET NULL).
    -   Row-Level Security (RLS) policies for user data isolation.
-   **API Error Handling & Resilience:**
    -   Handling of invalid inputs and server-side errors.
    -   Resilience mechanisms for the OpenRouter API integration (retries, timeouts).

### 2.2. Out-of-Scope Features (for this version)

-   **Spaced Repetition & Study Sessions:** Functionality is currently a placeholder and will not be tested beyond basic endpoint availability.
-   **Advanced Analytics & Insights:** These features are not yet implemented.
-   **Frontend Application:** This plan covers backend API testing only.
-   **Advanced Performance & Scalability Testing:** Comprehensive stress testing is deferred until after the MVP is functionally complete.
-   **Infrastructure & Deployment:** Testing related to the deployment pipeline, Kubernetes, or cloud infrastructure is not covered.

---

## 3. Types of Tests

A multi-layered testing approach will be adopted:

| Test Type              | Description                                                                                                                                                              | Tools                             | Scope                                                                                                |
| ---------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | --------------------------------- | ---------------------------------------------------------------------------------------------------- |
| **Unit Tests**         | Test individual components (classes, methods) in isolation. Focus on domain logic in entities, command/query handlers, mappers, and utility classes.                      | JUnit 5, Mockito                  | Domain entities, Command/Query handlers, MapStruct mappers, `OpenRouterService` (with mocked client). |
| **Integration Tests**  | Test the interaction between components. Focus on API controllers, database repositories, and the full CQRS pipeline.                                                     | Spring Boot Test, Testcontainers | Controller endpoints, JPA repositories (`@DataJpaTest`), full request flow from controller to DB.     |
| **API / Contract Tests** | Verify that the API conforms to the `open-api.yaml` specification. Test request/response schemas, status codes, and headers.                                            | Postman, REST Assured             | All public and protected API endpoints.                                                              |
| **Security Tests**     | Focus specifically on vulnerabilities and security features.                                                                                                             | Spring Security Test, SQL Queries | JWT validation, endpoint access control (role-based), RLS policy enforcement, GDPR cascade deletes.   |
| **Component Tests**    | Test the integration with the external OpenRouter service. These tests will make real API calls to a controlled environment.                                             | JUnit 5, REST Assured             | `OpenRouterApiClient` against the actual OpenRouter API. To be run manually or in a separate CI stage. |
| **Manual / E2E Tests** | Manual end-to-end testing of complete user flows using an API client like Postman.                                                                                        | Postman                           | Full user registration -> AI generation -> flashcard approval flow.                                  |

---

## 4. Test Scenarios for Key Functionalities

### 4.1. User Authentication & Security

-   **TC-AUTH-01:** Verify successful user registration with valid data.
-   **TC-AUTH-02:** Verify user registration fails with duplicate username/email.
-   **TC-AUTH-03:** Verify successful user login with correct credentials and reception of a valid JWT.
-   **TC-AUTH-04:** Verify login fails with incorrect credentials.
-   **TC-SEC-01:** Verify that protected endpoints (e.g., `GET /flashcards`) return 401/403 for unauthenticated users.
-   **TC-SEC-02:** Verify that protected endpoints are accessible with a valid JWT Bearer token.
-   **TC-SEC-03:** Verify API access is denied with an expired or malformed JWT.
-   **TC-RLS-01:** (Critical) Verify that User A cannot access any data (flashcards, AI sessions) belonging to User B by directly calling the API with valid IDs. This requires setting the `app.current_user_id` session variable.
-   **TC-GDPR-01:** (Critical) Verify that deleting a user account cascades to delete all associated user roles, AI sessions, and flashcards.

### 4.2. AI Flashcard Generation

-   **TC-AI-01:** Verify successful creation of an AI generation session with valid input text (1,000-10,000 characters).
-   **TC-AI-02:** Verify session creation fails with input text outside the character limits.
-   **TC-AI-03:** Verify the status of a completed session can be retrieved correctly.
-   **TC-AI-04:** Verify that suggestions can be retrieved from a `COMPLETED` session.
-   **TC-AI-05:** Verify that an empty list of suggestions is returned for a `PENDING` or `FAILED` session.
-   **TC-AI-06:** Verify successful approval of AI suggestions without any edits. Check that `Flashcard` entities are created with `source=AI`.
-   **TC-AI-07:** Verify successful approval of AI suggestions with content edits. Check that `Flashcard` entities are created with `source=AI_USER`.
-   **TC-AI-08:** Verify the `acceptedCount` on the `AiGenerationSession` is updated correctly after approval.
-   **TC-AI-09 (Resilience):** Verify the system handles an OpenRouter API failure gracefully by creating a session with `FAILED` status.

### 4.3. Manual Flashcard Management

-   **TC-CRUD-01:** Verify successful creation of a manual flashcard. Check that the `source` is set to `USER`.
-   **TC-CRUD-02:** Verify a user can retrieve their own list of flashcards.
-   **TC-CRUD-03:** Verify pagination (`page`, `size`) and sorting (`sort`) work correctly on the `GET /flashcards` endpoint.
-   **TC-CRUD-04:** Verify a user can successfully update their own flashcard.
-   **TC-CRUD-05:** Verify a user can successfully delete their own flashcard.

---

## 5. Test Environment

-   **Local Environment:** Developers' machines running PostgreSQL via Docker Compose, as specified in `docker-compose.yml`.
-   **CI/CD Environment:** Jenkins/GitHub Actions pipeline. Integration tests will run against a PostgreSQL database provisioned by **Testcontainers** to ensure a clean and consistent environment for every test run.
-   **Staging Environment:** A dedicated server mirroring the production setup where manual E2E tests and component tests against the real OpenRouter API will be conducted.

---

## 6. Testing Tools

-   **Unit & Integration Testing:** JUnit 5, Mockito, Spring Boot Test Framework
-   **Database Testing:** Testcontainers for PostgreSQL
-   **API Testing:** Postman (manual), REST Assured (automated)
-   **Build & Execution:** Apache Maven
-   **CI/CD:** GitHub Actions / Jenkins
-   **Code Coverage:** JaCoCo

---

## 7. Test Schedule

| Phase                     | Start Date | End Date   | Responsibilities   |
| ------------------------- | ---------- | ---------- | ------------------ |
| **Unit Test Development** | Ongoing    | Continuous | Development Team   |
| **Integration Test Dev.** | Ongoing    | Continuous | Development Team   |
| **API & Security Testing**  | Sprint End | Sprint End | QA Team            |
| **Component Testing**     | Bi-weekly  | Bi-weekly  | QA Team            |
| **Regression Testing**    | Pre-release| Pre-release| QA Team, Automated |
| **Manual E2E Testing**    | Pre-release| Pre-release| QA Team            |

---

## 8. Test Acceptance Criteria

### 8.1. Entry Criteria

-   All core features for the current sprint are code-complete.
-   Unit and integration tests are written and passing in the CI pipeline.
-   The application is successfully deployed to the Staging environment.

### 8.2. Exit Criteria

-   **100%** of test cases for Critical and High priority features have passed.
-   **95%** of all planned test cases have passed.
-   No open **Blocker** or **Critical** severity bugs.
-   Code coverage meets the project target (e.g., >80% for domain and application layers).
-   All security vulnerabilities identified during testing have been addressed.

---

## 9. Roles and Responsibilities

-   **Development Team:**
    -   Writing and maintaining unit and integration tests.
    -   Fixing bugs reported by the QA team.
    -   Ensuring CI pipeline health.
-   **QA Team:**
    -   Developing and executing the test plan (API, Security, Component, Manual tests).
    -   Reporting, tracking, and verifying bugs.
    -   Maintaining the automated regression test suite.
    -   Providing the final sign-off for release.
-   **Project Manager:**
    -   Overseeing the testing schedule and resource allocation.
    -   Triaging bugs and prioritizing fixes with the development and QA teams.

---

## 10. Bug Reporting Procedures

-   **Tool:** JIRA will be used for bug tracking.
-   **Process:**
    1.  Bugs found during testing will be logged in JIRA with a detailed description, steps to reproduce, expected vs. actual results, environment details, and relevant logs/screenshots.
    2.  Bugs will be assigned a priority and severity level.
        -   **Severity:** Blocker, Critical, Major, Minor, Trivial.
        -   **Priority:** High, Medium, Low.
    3.  The bug will be assigned to the development team for fixing.
    4.  Once fixed, the bug will be moved to a "Ready for QA" state.
    5.  The QA team will re-test the issue on the Staging environment. If verified, the bug will be closed. If not, it will be reopened with additional comments.

---
