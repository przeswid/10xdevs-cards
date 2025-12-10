You are a GitHub Actions specialist in the @tech-stack.md stack.

Create a "pull-request.yml" scenario based on Github actions.

**Workflow:**
The "pull-request.yml" scenario should work as follows:

- Code linting
- Then two parallel jobs: unit-test
- Finally: status-comment (a comment on the PR with the overall status)

**Additional notes:**
- status-comment runs only when the previous set of 3 jobs passes successfully
- In the e2e job, fetch browsers according to @playwright.config.ts
- Collect coverage from unit tests