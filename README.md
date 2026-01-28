# 10xDevs Cards Monorepo

A full-stack application for AI-powered flashcard generation and spaced repetition learning. This monorepo contains both the Spring Boot backend and Astro frontend applications.

## Table of Contents

- [Project Overview](#project-overview)
- [Repository Structure](#repository-structure)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [CI/CD Pipelines](#cicd-pipelines)
- [Environment Variables](#environment-variables)
- [Testing](#testing)
- [Detailed Documentation](#detailed-documentation)

## Project Overview

10xDevs Cards is an educational platform that leverages AI to help users create and study flashcards efficiently. Users can paste text from study materials, and the application uses LLM models to generate intelligent flashcard suggestions. The platform also implements spaced repetition algorithms to optimize the learning process.

### Key Features

- **AI-Powered Generation**: Paste text and let AI create flashcard suggestions
- **Manual Flashcard Management**: Full CRUD operations for custom flashcards
- **Spaced Repetition**: Study sessions based on proven learning algorithms
- **Secure Authentication**: JWT-based authentication with user data isolation
- **Modern UI**: Server-side rendered Astro frontend with React interactive components

## Repository Structure

```
10xdevs-monorepo/
├── backend/                    # Spring Boot API (Java 21)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Application source code
│   │   │   └── resources/     # Configuration files
│   │   └── test/              # Unit and integration tests
│   ├── Dockerfile             # Container image definition
│   ├── docker-compose.yml     # Local PostgreSQL setup
│   ├── pom.xml                # Maven dependencies
│   └── README.md              # Backend documentation
│
├── frontend/                   # Astro SSR Application
│   ├── src/
│   │   ├── components/        # UI components (Astro + React)
│   │   ├── lib/               # API clients, hooks, utilities
│   │   ├── pages/             # Routes and API endpoints
│   │   └── layouts/           # Page layouts
│   ├── tests/                 # Playwright E2E tests
│   ├── docker-compose.e2e.yml # E2E test infrastructure
│   └── README.md              # Frontend documentation
│
├── .github/
│   └── workflows/             # GitHub Actions CI/CD
│       ├── backend-ci.yml     # Backend continuous integration
│       ├── backend-cd.yml     # Backend deployment
│       ├── backend-pr.yml     # Backend pull request checks
│       └── frontend-playwright.yml  # Frontend E2E tests
│
└── README.md                  # This file
```

## Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Programming language |
| Spring Boot | 3.5.7 | Application framework |
| PostgreSQL | 15+ | Primary database |
| Spring Security + JWT | - | Authentication |
| Liquibase | - | Database migrations |
| Pipelinr | 0.11 | CQRS implementation |
| MapStruct | 1.5.5 | Object mapping |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| Astro | 5 | Web framework with SSR |
| React | 19 | Interactive components |
| TypeScript | 5 | Type safety |
| Tailwind CSS | 4 | Styling |
| Shadcn/ui | - | Component library |
| Axios | - | HTTP client |
| Zod | - | Schema validation |

### Testing
| Tool | Scope |
|------|-------|
| JUnit 5 + Mockito | Backend unit tests |
| Testcontainers | Backend integration tests |
| RestAssured | API testing |
| Vitest | Frontend unit tests |
| Playwright | E2E tests |

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Node.js 22.14.0** (see `frontend/.nvmrc`)
- **Docker** and **Docker Compose**
- **Maven** (or use the included wrapper)

### Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd 10xdevs-monorepo
   ```

2. **Start the backend database**
   ```bash
   cd backend
   docker compose up -d
   ```

3. **Run the backend**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
   The API will be available at `http://localhost:8080`

4. **Run the frontend** (in a new terminal)
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   The application will be available at `http://localhost:3000`

### Running with Docker (E2E Setup)

To run the full stack with Docker:

```bash
cd frontend
npm run test:e2e:backend:up
```

This starts:
- PostgreSQL on port `5433`
- Spring Boot backend on port `8080`

To stop:
```bash
npm run test:e2e:backend:down
```

## Development Workflow

### Backend Development

```bash
cd backend

# Compile
./mvnw clean compile

# Run tests
./mvnw test

# Run with hot reload
./mvnw spring-boot:run

# Build JAR
./mvnw clean package -DskipTests
```

### Frontend Development

```bash
cd frontend

# Install dependencies
npm install

# Start dev server (port 3000)
npm run dev

# Build for production
npm run build

# Run linting
npm run lint

# Format code
npm run format
```

## CI/CD Pipelines

The monorepo uses path-filtered GitHub Actions workflows to run CI/CD only for changed components.

### Backend Pipelines

| Workflow | Trigger | Description |
|----------|---------|-------------|
| `backend-ci.yml` | Push to `main` | Build, test, generate coverage report, create JAR artifact |
| `backend-pr.yml` | Pull request to `main` | Lint, unit tests, integration tests with PostgreSQL service |
| `backend-cd.yml` | Manual dispatch | Build Docker image, deploy to staging/production |

### Frontend Pipelines

| Workflow | Trigger | Description |
|----------|---------|-------------|
| `frontend-playwright.yml` | Push/PR to `main` | Install deps, run Playwright E2E tests, upload report |

### Path Filtering

Workflows only run when relevant files change:
- Backend workflows: `backend/**` changes
- Frontend workflows: `frontend/**` changes

## Environment Variables

### Backend

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `password` | Database password |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/tenx_cards` | Database connection URL |
| `OPENROUTER_API_KEY` | - | OpenRouter API key for AI features |
| `APP_URL` | `http://localhost:8080` | Application URL for OpenRouter headers |

### Frontend

| Variable | Default | Description |
|----------|---------|-------------|
| `PUBLIC_API_URL` | `http://localhost:8080` | Backend API URL |

### GitHub Actions Secrets

| Secret | Used By | Description |
|--------|---------|-------------|
| `OPENROUTER_API_KEY` | Frontend E2E tests | Required for AI generation tests |

## Testing

### Backend Tests

```bash
cd backend

# All tests
./mvnw test

# Specific test class
./mvnw test -Dtest=CardsApplicationTests

# Integration tests (requires Docker for Testcontainers)
./mvnw failsafe:integration-test failsafe:verify
```

Coverage target: **80%+** (JaCoCo)

### Frontend Tests

```bash
cd frontend

# Unit tests
npm run test

# Unit tests with coverage
npm run test:coverage

# E2E tests (full suite with Docker backend)
npm run test:e2e

# E2E tests only (backend must be running)
npm run test:e2e:run

# Single E2E test file
npx playwright test tests/us001-registration-happy-path.spec.ts
```

## Detailed Documentation

For more detailed information about each component:

- **[Backend Documentation](backend/README.md)** - Architecture, API endpoints, DDD patterns
- **[Frontend Documentation](frontend/README.md)** - Component structure, state management, styling

### AI Development Support

Both projects include AI assistant configuration files:
- `backend/.claude/CLAUDE.md` - Backend development guidelines
- `frontend/CLAUDE.md` - Frontend development guidelines

## API Overview

### Public Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/auth/register` | User registration |
| `POST` | `/auth/login` | User authentication |

### Protected Endpoints (JWT required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/flashcards` | Get user's flashcards |
| `POST` | `/flashcards` | Create flashcard |
| `PUT` | `/flashcards/{id}` | Update flashcard |
| `DELETE` | `/flashcards/{id}` | Delete flashcard |
| `POST` | `/ai-generation/sessions` | Start AI generation session |
| `GET` | `/ai-generation/sessions/{id}` | Get session status |
| `GET` | `/ai-generation/sessions/{id}/suggestions` | Get AI suggestions |
| `POST` | `/ai-generation/sessions/{id}/approve` | Approve suggestions |

## Architecture Highlights

### Backend (DDD + Hexagonal + CQRS)
- **Domain Layer**: Rich domain entities with business logic
- **Application Layer**: Commands and queries via Pipelinr
- **Infrastructure Layer**: JPA entities, repositories, mappers
- **Presentation Layer**: REST controllers

### Frontend (Astro SSR + React)
- **Server-side rendering**: Astro with Node adapter
- **Hybrid components**: Astro for pages, React for interactivity
- **API Layer**: Axios client with JWT interceptors
- **State Management**: React Context for auth and generation state

## License

See individual project directories for license information.

---

For questions or issues, please open a GitHub issue in this repository.
