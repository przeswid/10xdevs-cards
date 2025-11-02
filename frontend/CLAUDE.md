# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

- `npm run dev` - Start development server on port 3000
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm run lint:fix` - Fix ESLint issues automatically
- `npm run format` - Format code with Prettier

## Tech Stack

- **Astro 5** - Static site generation framework with SSR capabilities
- **React 19** - UI library for interactive components only
- **TypeScript 5** - Type-safe JavaScript with strict configuration
- **Tailwind CSS 4** - Utility-first CSS framework via Vite plugin
- **Shadcn/ui** - Component library configured with New York style
- **Node.js v22.14.0** - Required runtime version (see .nvmrc)

## Project Origin

This project is based on the **10x Astro Starter** template, customized to work with a Spring Boot backend instead of the template's default Supabase integration.

## Backend

This project is a **frontend-only application**. Backend functionality is provided by a separate Spring Boot application (not Supabase).

## Architecture

This is an Astro project configured for static site generation with React integration. Key architectural decisions:

- **Static generation**: Uses Astro's static output mode (`output: "static"`)
- **Hybrid rendering**: Astro components for static content, React for interactivity
- **Path aliases**: `@/*` maps to `./src/*` for clean imports
- **Component organization**: Shadcn/ui components in `src/components/ui/`
- **API integration**: Frontend communicates with separate Spring Boot backend via API client services

## Project Structure

```
src/
├── components/          # UI components
│   ├── ui/             # Shadcn/ui components
│   └── *.astro         # Astro components
├── layouts/            # Astro layouts
├── pages/              # File-based routing
├── lib/               # Utilities and API client services
│   └── utils.ts       # cn() helper for class merging
└── styles/            # Global styles
```

## Code Conventions

### Component Usage
- Use **Astro components** (.astro) for static content and layouts
- Use **React components** (.tsx) only when interactivity is required
- Extract API client logic and utilities into `src/lib/` services

### Styling
- Use `cn()` utility from `@/lib/utils` for conditional class merging
- Follow Tailwind 4 patterns with utility-first approach (imported via `@import "tailwindcss"`)
- Leverage CSS custom properties for theming with oklch color space
- Dark mode via custom variant: `@custom-variant dark (&:is(.dark *))`
- Theme configuration in components.json (New York style, neutral base color)

### TypeScript
- Extends Astro's strict TypeScript configuration
- JSX configured for React with automatic runtime
- Use absolute imports with `@/*` paths

### Linting
- Comprehensive ESLint setup with React, Astro, and accessibility rules
- React Compiler plugin enabled for optimization
- Prettier integration for code formatting
- Pre-commit hooks via husky and lint-staged

## Backend Integration

This frontend communicates with a separate Spring Boot backend:
- Create API client services in `src/lib/` for backend communication
- Use proper TypeScript types for API requests/responses
- Handle loading states and errors appropriately in components
- Consider environment variables for backend API URL configuration

## AI Development Support

This project includes comprehensive AI assistant configurations:
- **Cursor IDE rules**: Located in `.cursor/rules/` (8 specialized .mdc files)
- **GitHub Copilot instructions**: Found in `.github/copilot-instructions.md`
- **Windsurf configuration**: Available in `.windsurfrules`

These files contain detailed guidelines for code patterns, accessibility, API integration, and framework-specific best practices.

## Key Guidelines

- Prioritize error handling with early returns and guard clauses
- Use React hooks pattern (functional components only)
- Never use Next.js directives like "use client"
- Implement proper ARIA attributes for accessibility
- Extract API client logic and utilities into services in `src/lib/`