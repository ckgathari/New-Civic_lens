# Test Coverage Configuration Guide

## Backend Testing Setup

### Step 1: Update pom.xml
Replace the current `backend/pom.xml` with the enhanced version that includes testing dependencies:

```bash
cp backend/pom-enhanced.xml backend/pom.xml
```

**Key testing dependencies added:**
- `spring-boot-starter-test` - JUnit 5, Mockito, AssertJ
- `spring-security-test` - Security test utilities
- `rest-assured` - RESTful API testing
- `testcontainers` - PostgreSQL container for integration tests
- `jacoco-maven-plugin` - Code coverage reporting

### Step 2: Install Maven Dependencies

```bash
cd backend
mvn clean install
```

### Step 3: Run Backend Tests

**Run all tests with coverage:**
```bash
mvn clean test jacoco:report
```

**Run specific test class:**
```bash
mvn test -Dtest=PollServiceTest
```

**Run tests matching pattern:**
```bash
mvn test -Dtest=*Controller*
```

**View coverage report:**
Open `backend/target/site/jacoco/index.html` in browser

### Step 4: Coverage Goals

**Target coverage by component:**
- **Security**: JwtTokenProvider (100%), SecurityConfig (100%)
- **Services**: PollService (100%), AspirantService (100%)
- **Controllers**: PollController (100%), AspirantAdminController (100%)
- **Overall**: 95% minimum across codebase

---

## Frontend Testing Setup

### Step 1: Install Test Dependencies

```bash
cd frontend
npm install --save-dev vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event jsdom
npm install --save-dev @vitest/ui @vitest/coverage-v8
npm install --save-dev msw @testing-library/jest-dom happy-dom
```

### Step 2: Update package.json Scripts

Add these scripts to `frontend/package.json`:

```json
{
  "scripts": {
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:run": "vitest run",
    "test:coverage": "vitest run --coverage",
    "test:watch": "vitest --watch",
    "test:debug": "vitest --inspect-brk --inspect --single-thread"
  }
}
```

### Step 3: Configure Vitest

The configuration files are already created:
- `frontend/vitest.config.js` - Main Vitest configuration
- `frontend/vitest.setup.js` - Global test setup

### Step 4: Run Frontend Tests

**Run tests in watch mode (development):**
```bash
npm test
```

**Run all tests once:**
```bash
npm run test:run
```

**Run tests with UI dashboard:**
```bash
npm run test:ui
```

**Generate coverage report:**
```bash
npm run test:coverage
```

**Debug tests:**
```bash
npm run test:debug
```

---

## Test File Structure

### Backend Test Organization

```
backend/src/test/java/com/civiclens/
├── security/
│   ├── JwtTokenProviderTest.java
│   └── JwtAuthenticationFilterTest.java
├── service/
│   ├── PollServiceTest.java
│   ├── AspirantServiceTest.java
│   ├── AuthServiceTest.java
│   └── CommentServiceTest.java
├── controller/
│   ├── PollControllerTest.java
│   ├── AspirantAdminControllerTest.java
│   ├── AuthControllerTest.java
│   └── CommentControllerTest.java
├── repository/
│   ├── PollVoteRepositoryTest.java
│   └── UserRepositoryTest.java
└── integration/
    ├── AuthIntegrationTest.java
    ├── PollIntegrationTest.java
    └── AspirантIntegrationTest.java
```

### Frontend Test Organization

```
frontend/src/__tests__/
├── pages/
│   ├── LoginPage.test.jsx
│   ├── Dashboard.test.jsx
│   ├── AspirantManagement.test.jsx
│   ├── SignupPage.test.jsx
│   └── LeaderPage.test.jsx
├── components/
│   ├── SignupForm.test.jsx
│   ├── LocationSelector.test.jsx
│   └── Navbar.test.jsx
├── api/
│   └── apiClient.test.js
├── utils/
│   └── validation.test.js
└── integration/
    ├── auth-flow.test.jsx
    ├── voting-flow.test.jsx
    └── comment-flow.test.jsx
```

---

## Writing Tests: Best Practices

### Backend (Java/JUnit)

**Pattern for Unit Tests:**
```java
@DisplayName("ClassName - Feature Description")
class ClassNameTest {
    @Mock private Dependency dependency;
    @InjectMocks private ClassUnderTest sut;
    
    @BeforeEach
    void setUp() {
        // Initialize test data
    }
    
    @Test
    @DisplayName("Should do something when condition")
    void testDescriptiveNameSuccess() {
        // Arrange
        when(dependency.method()).thenReturn(value);
        
        // Act
        Result result = sut.methodUnderTest();
        
        // Assert
        assertEquals(expected, result);
        verify(dependency, times(1)).method();
    }
}
```

**Key Annotations:**
- `@ExtendWith(MockitoExtension.class)` - Enable Mockito
- `@Mock` - Create mock dependency
- `@InjectMocks` - Inject mocks into class under test
- `@BeforeEach` - Setup before each test
- `@Test` - Mark as test method
- `@DisplayName` - Human-readable test name

### Frontend (JavaScript/Vitest)

**Pattern for Component Tests:**
```javascript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

describe('ComponentName', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should do something', async () => {
    const user = userEvent.setup();
    render(<Component />);
    
    await user.click(screen.getByRole('button'));
    
    await waitFor(() => {
      expect(screen.getByText(/expected/i)).toBeInTheDocument();
    });
  });
});
```

**Best Practices:**
- Query by role, label, or accessible name
- Use `userEvent` instead of `fireEvent` for realistic interactions
- Use `waitFor` for async operations
- Mock external dependencies (APIs, localStorage, etc.)
- One assertion concept per test
- Descriptive test names

---

## Coverage Reports

### Backend Coverage Report

**Generate report:**
```bash
mvn jacoco:report
```

**View in browser:**
```bash
open target/site/jacoco/index.html  # macOS
xdg-open target/site/jacoco/index.html  # Linux
start target/site/jacoco/index.html  # Windows
```

**Key metrics:**
- Line coverage - % of lines executed
- Branch coverage - % of conditional branches taken
- Cyclomatic complexity - Code complexity measurement

### Frontend Coverage Report

**Generate report:**
```bash
npm run test:coverage
```

**View in browser:**
```bash
open coverage/index.html  # macOS
xdg-open coverage/index.html  # Linux
start coverage/index.html  # Windows
```

**Coverage thresholds (vitest.config.js):**
- Lines: 80%
- Functions: 80%
- Branches: 80%
- Statements: 80%

---

## CI/CD Integration

### GitHub Actions Workflow

**`.github/workflows/test.yml`:**
```yaml
name: Tests

on: [push, pull_request]

jobs:
  backend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - run: cd backend && mvn clean test jacoco:report
      - uses: codecov/codecov-action@v3
        with:
          files: ./backend/target/site/jacoco/jacoco.xml

  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
      - run: cd frontend && npm ci && npm run test:coverage
      - uses: codecov/codecov-action@v3
        with:
          files: ./frontend/coverage/coverage-final.json
```

---

## Troubleshooting

### Backend Issues

**"No tests found"**
- Verify test files end with `Test.java`
- Check package structure matches configuration
- Run: `mvn clean compile test`

**"Mockito mocks not working"**
- Add `@ExtendWith(MockitoExtension.class)`
- Verify `@Mock` and `@InjectMocks` are used correctly

**"Database connection errors in tests"**
- Add `@Testcontainers` annotation (if using Docker)
- Use embedded H2 database for unit tests
- Check PostgreSQL container is running for integration tests

### Frontend Issues

**"Tests not found"**
- Verify files end with `.test.js` or `.test.jsx`
- Check `vitest.config.js` includes pattern
- Run: `npm run test`

**"React Testing Library not working"**
- Import `render` and `screen` from `@testing-library/react`
- Use semantic queries (getByRole, getByLabelText)
- Wrap async operations in `waitFor`

**"API mocks not intercepting calls"**
- Verify MSW setup in test files
- Check Mock Service Worker server handlers

---

## Continuous Coverage Monitoring

**Set coverage thresholds:**
- Commit level: 85% minimum
- Release level: 95% minimum
- Critical paths: 100%

**Track coverage over time:**
- Use Codecov.io integration
- Generate historical reports
- Set coverage quality gates in CI

---

## Next Steps

1. ✅ Create test files (completed - initial test templates)
2. ⏳ Implement full test coverage for all controllers
3. ⏳ Implement full test coverage for all services
4. ⏳ Add integration tests for key workflows
5. ⏳ Generate coverage baseline report
6. ⏳ Set up CI/CD test automation
7. ⏳ Establish team testing standards
