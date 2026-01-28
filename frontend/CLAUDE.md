# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

- `npm run dev` - Start development server on port 3000
- `npm run build` - Build for production (outputs to `dist/`)
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm run lint:fix` - Fix ESLint issues automatically
- `npm run format` - Format code with Prettier

### E2E Testing (Playwright)

Requires the backend repository at `../10xdevs-cards-backend` with a Dockerfile.

- `npm run test:e2e` - Full E2E test suite (starts Docker backend, runs tests, tears down)
- `npm run test:e2e:run` - Run Playwright tests only (assumes backend is running)
- `npm run test:e2e:backend:up` - Start Docker backend services (PostgreSQL + Spring Boot)
- `npm run test:e2e:backend:down` - Stop and remove Docker backend services
- `npx playwright test tests/us001-registration-happy-path.spec.ts` - Run a single test file

Tests are located in `tests/` directory. The E2E setup uses Docker Compose to spin up PostgreSQL (port 5433) and Spring Boot backend (port 8080).

### Unit Testing (Vitest)

- `npm run test` - Run all unit tests
- `npm run test:watch` - Run tests in watch mode
- `npm run test:coverage` - Run tests with coverage report
- `npx vitest src/lib/validation/auth.test.ts` - Run a single test file

Unit tests are co-located with source files using `.test.ts` suffix in `src/lib/`. Test coverage includes validation schemas, services, and utilities.

## Tech Stack

- **Astro 5** with Node adapter for SSR (`output: "server"`)
- **React 19** - UI library for interactive components only
- **TypeScript 5** - Type-safe JavaScript with strict configuration
- **Tailwind CSS 4** - Utility-first CSS via Vite plugin
- **Shadcn/ui** - Component library (New York style, neutral base)
- **Axios** - HTTP client for backend API communication
- **Zod** - Schema validation
- **Node.js v22.14.0** - Required runtime (see .nvmrc)

## Architecture

This is an Astro SSR application that communicates with a separate Spring Boot backend.

### Rendering Strategy
- **Server-side rendering**: Uses `output: "server"` with Node adapter (standalone mode)
- **Hybrid components**: Astro for pages/layouts, React for interactive client components
- **Path aliases**: `@/*` maps to `./src/*`

### API Client Architecture

The frontend communicates with Spring Boot backend via Axios:

- `src/lib/api/client.ts` - Axios instance with interceptors for auth tokens and error handling
- `src/lib/api/auth.ts` - Authentication API calls (login, register)
- `src/lib/api/ai.ts` - AI generation API calls
- `src/lib/api/types.ts` - Shared TypeScript types for API requests/responses
- `src/lib/services/tokenService.ts` - JWT token storage and retrieval
- `src/lib/validation/` - Zod schemas for form validation

Backend URL configured via `PUBLIC_API_URL` env variable (defaults to `http://localhost:8080`).

### State Management

React Context is used for global state:
- `src/lib/context/AuthContext.tsx` - Authentication state
- `src/lib/context/GenerationContext.tsx` - Flashcard generation workflow state

### Component Organization

- `src/components/ui/` - Shadcn/ui base components
- `src/components/auth/` - Authentication forms (LoginForm, RegisterForm)
- `src/components/generation/` - Flashcard generation UI
- `src/components/navigation/` - App header and navigation
- `src/lib/hooks/` - Custom React hooks

### Pages

- `/` - Landing page
- `/login` - User login
- `/register` - User registration
- `/generate` - AI flashcard generation
- `/flashcards` - Flashcard management

### API Routes

Server-side API routes in `src/pages/api/`:
- `POST /api/auth/set-cookie` - Set auth cookie after login
- `POST /api/auth/logout` - Clear auth cookie

## Code Conventions

### Component Usage
- Use **Astro components** (.astro) for pages and layouts
- Use **React components** (.tsx) only when client-side interactivity is required
- Never use Next.js directives like "use client"

### Styling
- Use `cn()` utility from `@/lib/utils` for conditional class merging
- Dark mode via custom variant: `@custom-variant dark (&:is(.dark *))`

### Error Handling
- Prioritize early returns and guard clauses
- Handle errors at the beginning of functions
- Use Zod for input validation in API routes and forms

### React Patterns
- Functional components with hooks only
- Extract reusable logic into custom hooks in `src/lib/hooks/`
- Use React Context for cross-component state

### API Routes
- Use uppercase HTTP method names: `POST`, `GET`
- Add `export const prerender = false` for API routes
- Validate input with Zod schemas