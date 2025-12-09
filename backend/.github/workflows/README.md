# GitHub Actions CI/CD Workflows

This directory contains the CI/CD pipeline configuration for the 10xDevs Cards Backend project.

## Workflows Overview

### 1. CI Pipeline (`ci.yml`)

**Triggers:**
- Push to `main` branch
- Pull requests to `main` branch
- Manual dispatch via GitHub Actions UI

**Jobs:**

#### `build-and-test`
Builds the application and runs the complete test suite.

**Steps:**
1. Checkout code
2. Set up JDK 21 with Maven cache
3. Generate OpenAPI code from `open-api.yaml`
4. Compile project with Maven
5. Run all tests (unit, integration, E2E) with Testcontainers
6. Check JaCoCo code coverage (enforces 80% threshold)
7. Upload coverage report as artifact (30-day retention)
8. Package application as JAR
9. Upload JAR artifact (7-day retention)

**Requirements:**
- No external dependencies
- Testcontainers handles PostgreSQL automatically
- Tests run in parallel (4 threads)

#### `code-quality`
Runs architecture validation tests.

**Steps:**
1. Checkout code
2. Set up JDK 21
3. Run ArchUnit tests (DDD pattern validation)
4. Download coverage report
5. Generate coverage summary in GitHub step summary

**What Gets Tested:**
- Unit tests (Mockito + AssertJ)
- Integration tests (Spring Boot Test + Testcontainers)
- E2E tests (RestAssured)
- Architecture tests (ArchUnit - DDD/hexagonal architecture rules)

**Code Coverage:**
- Minimum: 80% line and branch coverage
- Excludes: generated code, configuration, DTOs, JPA entities

### 2. CD Pipeline (`cd.yml`)

**Triggers:**
- Manual dispatch only (via GitHub Actions UI)

**Inputs:**
- `environment`: Choose between `staging` or `production`

**Jobs:**

#### `deploy`
Builds Docker image and prepares for deployment.

**Steps:**
1. Checkout code
2. Set up JDK 21
3. Build application JAR (skips tests)
4. Build Docker image with commit SHA tag
5. Log deployment summary to GitHub step summary

**Current State:**
- Builds Docker image locally
- Tagged with commit SHA and `latest`
- Deployment steps are commented out (ready for configuration)

**Deployment Options** (commented, ready to enable):
- Docker Hub push
- AWS ECS deployment
- Kubernetes deployment

## Usage

### Running CI Pipeline

**Automatic:**
```bash
git push origin main  # Triggers CI automatically
```

**Manual:**
1. Go to Actions tab in GitHub
2. Select "CI Pipeline"
3. Click "Run workflow"
4. Select branch and click "Run workflow"

### Running CD Pipeline

**Manual Only:**
1. Go to Actions tab in GitHub
2. Select "CD Pipeline"
3. Click "Run workflow"
4. Select environment (staging/production)
5. Click "Run workflow"

## Artifacts

### Coverage Report
- **Location:** `target/site/jacoco/`
- **Retention:** 30 days
- **Contents:** HTML coverage reports, XML data
- **Access:** Download from workflow run page

### Application JAR
- **Location:** `target/*.jar`
- **Retention:** 7 days
- **Contents:** Executable Spring Boot JAR
- **Access:** Download from workflow run page

## Environment Setup

### Required Secrets (for CD)

When configuring deployment, add these secrets in GitHub Settings > Secrets and variables > Actions:

**For Docker Hub:**
```
DOCKER_USERNAME
DOCKER_PASSWORD
```

**For AWS ECS:**
```
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
```

**For Application:**
```
DB_USERNAME
DB_PASSWORD
JWT_SECRET
OPENROUTER_API_KEY
```

### Environment Configuration

Create GitHub Environments (Settings > Environments):
- `staging`
- `production`

Configure protection rules:
- Required reviewers for production
- Branch protection for main

## Monitoring

### Workflow Status

Check workflow status:
- Green checkmark: All jobs passed
- Red X: At least one job failed
- Yellow circle: Workflow running

### Coverage Reports

1. Navigate to completed workflow run
2. Scroll to "Artifacts" section
3. Download "jacoco-coverage-report"
4. Open `index.html` in browser

### Test Results

Test output is visible in workflow logs:
- Expand "Run tests with coverage" step
- View detailed test output
- See coverage percentages

## Customization

### Modify Test Execution

Edit `ci.yml`:
```yaml
- name: Run tests with coverage
  run: ./mvnw test -B -Dtest=SpecificTest
```

### Change Coverage Threshold

Edit `pom.xml`:
```xml
<minimum>0.80</minimum>  <!-- Change to desired percentage -->
```

### Add Deployment Target

Edit `cd.yml` and uncomment relevant section:
- Docker Hub
- AWS ECS
- Kubernetes
- Or add custom deployment steps

### Parallel Test Execution

Current: 4 threads (configured in `pom.xml`)

To change, edit `pom.xml`:
```xml
<threadCount>4</threadCount>  <!-- Adjust as needed -->
```

## Troubleshooting

### Tests Failing Locally But Pass in CI
- Check Java version (must be 21)
- Verify Docker is running (Testcontainers)
- Clean Maven cache: `./mvnw clean`

### Coverage Threshold Not Met
- View detailed report in artifacts
- Identify uncovered lines
- Add missing tests
- Or adjust threshold in `pom.xml`

### Docker Build Fails
- Verify `Dockerfile` exists in root
- Check Docker daemon status
- Review build logs in workflow

### Deployment Fails
- Verify secrets are configured
- Check environment exists
- Review deployment logs
- Validate target infrastructure

## Best Practices

1. **Always run CI before merging PRs**
2. **Review coverage reports regularly**
3. **Keep `main` branch protected**
4. **Use manual deployment for production**
5. **Tag releases for production deployments**
6. **Monitor workflow execution times**
7. **Keep artifacts clean (auto-expire in 7-30 days)**

## Maintenance

### Update Java Version
Edit both workflows:
```yaml
java-version: '21'  # Update to desired version
```

Also update `pom.xml`:
```xml
<java.version>21</java.version>
```

### Update Dependencies
Maven dependencies cached automatically by `setup-java` action.

To force cache refresh:
- Delete cache via Settings > Actions > Caches
- Or change cache key in workflow

### Workflow Performance

Current timings (approximate):
- `build-and-test`: 3-5 minutes
- `code-quality`: 1-2 minutes
- `deploy`: 2-4 minutes

Optimization tips:
- Maven cache enabled (faster dependency downloads)
- Parallel test execution (4 threads)
- Testcontainers reuses containers when possible
- Skip tests in package step (`-DskipTests`)

## Support

For issues with:
- **Workflows:** Check Actions tab logs
- **Tests:** Run locally with `./mvnw test`
- **Coverage:** Generate locally with `./mvnw jacoco:report`
- **Docker:** Test build locally with `docker build .`
