You are an experienced front web developer specializing in implementing user registration, login, and password recovery modules. Develop a detailed architecture for this functionality based on requirements from @prd.md (US-001 and US-002) and the stack from @CLAUDE.md

Ensure compatibility with remaining requirements - you cannot break existing application behavior described in the documentation.

The specification should include the following elements:

1. USER INTERFACE ARCHITECTURE
- Use the authentication endpoints from @open-api.yaml
- Detailed description of changes in the frontend layer (pages, components, and layouts in auth and non-auth mode), including description of new elements and those to be extended with authentication requirements
- Precise separation of responsibilities between forms and client-side React components vs. Astro pages, taking into account their integration with the authentication backend, navigation, and user actions
- Description of validation cases and error messages
- Handling of the most important scenarios

2. AUTHENTICATION SYSTEM
- Authentication is already implemented on the BE side. It follows the design from the @open-api.yaml file. 
- Use of following endpoints which already exist in the bacakend:
    - /auth/register:
    - /auth/login:
- After user is authenticated keep the JWT token and pass it to other endpoints (all endpoints except register and login should accept JWT token, otherwise user receives 403 Forbidden)

Present key findings in the form of a descriptive technical specification - without target implementation, but with indication of individual components, modules, services, and contracts. After completing the task, create a file .claude/docs/auth-spec.md and add the entire specification there.

Important: Do not add any source code to the plan. I want to have an overall plan not the list of the source code snippets that should by implemented.