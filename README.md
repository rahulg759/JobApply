# Naukri Automation Framework

Playwright + Java automation framework for Naukri.com job search and application.

## Tech Stack

- **Java 17+** with **Maven**
- **Playwright** for browser automation
- **JUnit 5** as test runner
- **Allure** for reporting
- **Log4j2** for logging
- **dotenv-java** for secure credential management

## Setup

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- Node.js (for Playwright browser binaries - managed automatically)

### Quick Start

1. **Clone the repo:**
   ```bash
   git clone <repo-url>
   cd naukri-automation
   ```

2. **Install Playwright browsers:**
   ```bash
   mvn exec:java -e -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install"
   ```

3. **Configure credentials:**
   ```bash
   cp .env.example .env
   ```
   Edit `.env` with your Naukri credentials:
   ```
   NAUKRI_URL=https://www.naukri.com
   NAUKRI_EMAIL=your_email@example.com
   NAUKRI_PASSWORD=your_password
   RESUME_PATH=./resume.pdf
   ```

4. **Adjust configuration** in `config/config.properties` as needed:
   - Browser type, headless mode, slowMo
   - Search keywords and location
   - Experience level and date posted filters
   - Keyword matching threshold

5. **Place your resume** as `resume.pdf` in the project root (or update `RESUME_PATH`).

### Run Tests

```bash
# Run all tests (default: Chrome headed)
mvn clean test

# Run with specific browser
mvn clean test -Dbrowser=firefox
mvn clean test -Dbrowser=edge
mvn clean test -Dbrowser=chrome

# Run headless (for CI)
mvn clean test -Dbrowser=chrome -Dbrowser.headless=true

# Run a single test
mvn clean test -Dtest=JobSearchAndApplyTest

# Run smoke tests only
mvn clean test -Dgroups=smoke
```

### Generate Reports

```bash
# Generate Allure report
mvn allure:report

# Serve Allure report locally
mvn allure:serve
```

Reports are generated in `target/allure-report/`.

## Project Structure

```
├── config/
│   └── config.properties          # Non-sensitive configuration
├── src/main/java/com/naukri/automation/
│   ├── browser/
│   │   └── BrowserManager.java    # Playwright lifecycle management
│   ├── config/
│   │   ├── BrowserConfig.java     # Browser settings record
│   │   ├── BrowserType.java       # CHROME / EDGE / FIREFOX enum
│   │   └── ConfigManager.java     # Config resolution (system -> .env -> properties)
│   ├── engine/
│   │   ├── JobScorer.java         # Job scoring orchestrator
│   │   ├── KeywordMatcher.java    # Keyword matching logic
│   │   └── KeywordMatchResult.java# Match result record
│   ├── exception/
│   │   └── AutomationException.java
│   ├── listeners/
│   │   └── AllureLifecycleListener.java  # Auto-screenshot on failure
│   ├── models/
│   │   ├── JobListing.java        # Job data record
│   │   └── SearchFilters.java     # Filter criteria record
│   ├── pages/
│   │   ├── BasePage.java          # Shared page methods
│   │   ├── HomePage.java          # Post-login search page
│   │   ├── JobApplyPage.java      # Application modal handling
│   │   ├── JobSearchPage.java     # Job search, filters, listing
│   │   └── LoginPage.java         # Naukri login flow
│   └── utils/
│       ├── LoggerUtil.java
│       ├── ScreenshotHelper.java  # Screenshot capture + save
│       └── WaitHelper.java        # Explicit wait wrappers
└── src/test/java/com/naukri/automation/
    ├── BaseTest.java              # Test lifecycle hooks
    ├── LoginTest.java             # Login smoke test
    └── JobSearchAndApplyTest.java # Main search + apply flow
```

## Security

- Credentials are stored in `.env` (gitignored)
- Never commit `.env` to version control
- CI/CD: pass via secrets or `-D` system properties
  ```bash
  mvn clean test -DNAUKRI_EMAIL=${{ secrets.NAUKRI_EMAIL }} ...
  ```

## CI/CD Integration

### GitHub Actions

```yaml
- name: Run tests
  run: mvn clean test -Dbrowser=chrome -Dbrowser.headless=true
  env:
    NAUKRI_EMAIL: ${{ secrets.NAUKRI_EMAIL }}
    NAUKRI_PASSWORD: ${{ secrets.NAUKRI_PASSWORD }}
```

### Jenkins

Pass credentials as environment variables in the Jenkins job configuration.
