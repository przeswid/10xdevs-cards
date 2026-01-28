# 10x Astro Starter

A modern, opinionated starter template for building fast, accessible, and AI-friendly web applications with SSR and Spring Boot backend integration.

## Tech Stack

- [Astro](https://astro.build/) v5 - Modern web framework with SSR (Node adapter, standalone mode)
- [React](https://react.dev/) v19 - UI library for interactive components only
- [TypeScript](https://www.typescriptlang.org/) v5 - Type-safe JavaScript with strict configuration
- [Tailwind CSS](https://tailwindcss.com/) v4 - Utility-first CSS framework via Vite plugin
- [Shadcn/ui](https://ui.shadcn.com/) - Component library (New York style, neutral base)
- [Axios](https://axios-http.com/) - HTTP client for backend API communication
- [Zod](https://zod.dev/) - Schema validation
- [Vitest](https://vitest.dev/) - Unit testing framework
- [Playwright](https://playwright.dev/) - E2E testing framework

## Prerequisites

- Node.js v22.14.0 (as specified in `.nvmrc`)
- npm (comes with Node.js)

## Getting Started

1. Clone the repository:

```bash
git clone https://github.com/przeprogramowani/10x-astro-starter.git
cd 10x-astro-starter
```

2. Install dependencies:

```bash
npm install
```

3. Configure environment variables (optional):

Create a `.env` file in the project root if you need to override the default backend URL:

```env
PUBLIC_API_URL=http://localhost:8080
```

4. Run the development server:

```bash
npm run dev
```

The application will be available at `http://localhost:3000`.

**Note**: The application requires a Spring Boot backend to be running. For development, you can either:
- Run the backend locally on port 8080
- Use the E2E test setup: `npm run test:e2e:backend:up`

5. Build for production:

```bash
npm run build
```

## Available Scripts

### Development
- `npm run dev` - Start development server on port 3000
- `npm run build` - Build for production (outputs to `dist/`)
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm run lint:fix` - Fix ESLint issues automatically
- `npm run format` - Format code with Prettier

### Unit Testing (Vitest)
- `npm run test` - Run all unit tests
- `npm run test:watch` - Run tests in watch mode
- `npm run test:coverage` - Run tests with coverage report
- `npx vitest src/lib/validation/auth.test.ts` - Run a single test file

Unit tests are co-located with source files using `.test.ts` suffix in `src/lib/`.

### E2E Testing (Playwright)

**Requirements**: Backend repository at `../10xdevs-cards-backend` with a Dockerfile.

- `npm run test:e2e` - Full E2E test suite (starts Docker backend, runs tests, tears down)
- `npm run test:e2e:run` - Run Playwright tests only (assumes backend is running)
- `npm run test:e2e:backend:up` - Start Docker backend services (PostgreSQL + Spring Boot)
- `npm run test:e2e:backend:down` - Stop and remove Docker backend services
- `npx playwright test tests/us001-registration-happy-path.spec.ts` - Run a single test file

Tests are located in `tests/` directory. The E2E setup uses Docker Compose to spin up PostgreSQL (port 5433) and Spring Boot backend (port 8080).

## Project Structure

```
.
├── src/
│   ├── components/
│   │   ├── ui/           # Shadcn/ui base components
│   │   ├── auth/         # Authentication forms (LoginForm, RegisterForm)
│   │   ├── generation/   # Flashcard generation UI
│   │   └── navigation/   # App header and navigation
│   ├── layouts/          # Astro layouts
│   ├── lib/
│   │   ├── api/          # API client and backend communication
│   │   │   ├── client.ts # Axios instance with interceptors
│   │   │   ├── auth.ts   # Authentication API calls
│   │   │   ├── ai.ts     # AI generation API calls
│   │   │   └── types.ts  # Shared TypeScript types
│   │   ├── context/      # React Context providers
│   │   │   ├── AuthContext.tsx         # Authentication state
│   │   │   └── GenerationContext.tsx   # Flashcard generation state
│   │   ├── hooks/        # Custom React hooks
│   │   ├── services/     # Business logic (tokenService, etc.)
│   │   ├── validation/   # Zod schemas for validation
│   │   └── utils/        # Utility functions
│   ├── pages/            # Astro pages and routes
│   │   ├── api/          # API endpoints
│   │   │   └── auth/     # Auth API routes (set-cookie, logout)
│   │   ├── index.astro   # Landing page
│   │   ├── login.astro   # Login page
│   │   ├── register.astro # Registration page
│   │   ├── generate.astro # AI flashcard generation
│   │   └── flashcards.astro # Flashcard management
│   └── styles/           # Global styles
├── tests/                # E2E tests (Playwright)
└── public/               # Public assets
```

## Architecture

This is an Astro SSR application that communicates with a separate Spring Boot backend.

### Rendering Strategy
- **Server-side rendering**: Uses `output: "server"` with Node adapter (standalone mode)
- **Hybrid components**: Astro for pages/layouts, React for interactive client components
- **Path aliases**: `@/*` maps to `./src/*`

### API Client Architecture

The frontend communicates with Spring Boot backend via Axios:

- **API Client**: Axios instance with interceptors for auth tokens and error handling
- **Authentication**: JWT token storage and retrieval via tokenService
- **Validation**: Zod schemas for form and API validation
- **Backend URL**: Configured via `PUBLIC_API_URL` env variable (defaults to `http://localhost:8080`)

### State Management

React Context is used for global state:
- `AuthContext` - Authentication state (user, login, logout)
- `GenerationContext` - Flashcard generation workflow state

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

## AI Development Support

This project is configured with AI development tools to enhance the development experience. See [CLAUDE.md](CLAUDE.md) for detailed guidelines on:

- Development commands and testing
- Architecture and API client patterns
- Code conventions and best practices
- Component organization

### Cursor IDE

The project includes AI rules in `.cursor/rules/` directory that help Cursor IDE understand the project structure and provide better code suggestions.

### GitHub Copilot

AI instructions for GitHub Copilot are available in `.github/copilot-instructions.md`

### Windsurf

The `.windsurfrules` file contains AI configuration for Windsurf.

## Contributing

Please follow the AI guidelines and coding practices defined in the AI configuration files when contributing to this project.

## License

MIT
