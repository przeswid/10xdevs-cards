# Prompt for AI Assistant: E2E Testing Solution Implementation

**Objective:** Implement a complete End-to-End (E2E) testing solution in the frontend repository using Playwright and Docker Compose.

**Context:**
*   **Requesting User:** pswida-boldare
*   **Date:** 2025-11-22 09:10:48 UTC
*   **Frontend Stack:** Astro + React, TypeScript, Node.js
*   **Backend Stack:** Spring Boot + PostgreSQL, managed with Docker.

---

### Your Task

Your task is to configure the frontend repository to run E2E tests against a full application stack (frontend + backend). Follow these steps precisely.

#### Step 1: Initialize Playwright

In the root of the user's frontend repository, execute the following shell command to initialize Playwright:

```bash
npm init playwright@latest
```

When prompted by the interactive CLI, use the following answers:
- **Language:** `TypeScript`
- **Test folder name:** `tests`
- **Add GitHub Actions workflow:** `true`

This will create `playwright.config.ts`, a `tests/` directory with an example, and a GitHub Actions workflow file.

#### Step 2: Configure Playwright

Next, overwrite the generated `playwright.config.ts` with the following content. This configuration adds the `webServer` block, which tells Playwright how to start the frontend application for testing.

```typescript name=playwright.config.ts
import { defineConfig, devices } from '@playwright/test';

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  testDir: './tests',
  /* Run tests in files in parallel */
  fullyParallel: true,
  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: !!process.env.CI,
  /* Retry on CI only */
  retries: process.env.CI ? 2 : 0,
  /* Opt out of parallel tests on CI. */
  workers: process.env.CI ? 1 : undefined,
  /* Reporter to use. See https://playwright.dev/docs/test-reporters */
  reporter: 'html',

  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {
    /* Base URL to use in actions like `await page.goto('/')`. */
    baseURL: 'http://127.0.0.1:4173',

    /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
    trace: 'on-first-retry',
  },

  /* Configure projects for major browsers */
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
  ],

  /* Run your local dev server before starting the tests */
  webServer: {
    command: 'npm run build && npm run preview',
    url: 'http://127.0.0.1:4173',
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000, // 2 minutes timeout for build and start
  },
});
```

#### Step 3: Create Docker Compose File for Backend Services

In the **root of the frontend repository**, create a new file named `docker-compose.e2e.yml`. This file will define the backend and database services required for the E2E tests.

**Important:** Instruct the user that they **must** update the `context` path to point to their backend project's directory.

```yaml name=docker-compose.e2e.yml
services:
  # PostgreSQL Database Service for E2E tests
  db:
    image: postgres:15-alpine
    container_name: e2e_db
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=password
      - POSTGRES_DB=tenx_cards
    ports:
      - "5433:5432" # Use a non-default port to avoid conflicts
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - e2e-network

  # Spring Boot Backend Service for E2E tests
  backend:
    build:
      # CRITICAL: This path must be updated to point to the backend repository
      context: ../10xdevs-cards-backend
      dockerfile: Dockerfile
    container_name: e2e_backend
    depends_on:
      db:
        condition: service_healthy
    ports:
      - "8080:8080"
    environment:
      # Database configuration - matches backend's application.yaml
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/tenx_cards
      - DB_USERNAME=postgres
      - DB_PASSWORD=password
      # JWT configuration (uses defaults from application.yaml)
      # OpenRouter API key for AI flashcard generation
      - OPENROUTER_API_KEY=${OPENROUTER_API_KEY:-}
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 3s
      start_period: 60s
      retries: 3
    networks:
      - e2e-network

networks:
  e2e-network:
    driver: bridge
```

#### Step 4: Add Orchestration Scripts to `package.json`

First, execute this command to add necessary helper packages for orchestrating the test run:

```bash
npm install --save-dev wait-on npm-run-all
```

Next, modify the `scripts` section of the `package.json` file in the frontend repository. Add the following scripts:

```json
"scripts": {
  // ... (keep existing scripts like "dev", "build")
  "test:e2e:backend:up": "docker-compose -f docker-compose.e2e.yml up -d --build",
  "test:e2e:backend:down": "docker-compose -f docker-compose.e2e.yml down",
  "test:e2e:wait": "wait-on http://localhost:8080/actuator/health",
  "test:e2e:run": "playwright test",
  "test:e2e": "npm-run-all -s test:e2e:backend:up test:e2e:wait test:e2e:run -c test:e2e:backend:down"
}
```
*Make sure to merge these scripts into the existing `scripts` object, not replace it.*

#### Final Instruction to User

Inform the user that the setup is complete. To run the entire E2E test suite, they need to:
1.  Ensure Docker Desktop is running.
2.  Update the backend path in `docker-compose.e2e.yml`.
3.  Run the master command from the frontend repository:
    ```bash
    npm run test:e2e
    ```
Advise them that the next step is to replace the example test in `tests/example.spec.ts` with a real test case for a critical user flow.