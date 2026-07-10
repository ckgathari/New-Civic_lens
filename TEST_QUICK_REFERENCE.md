# Test Execution Quick Reference

## 🚀 One-Line Commands

### Backend Tests

```bash
# Install & compile tests
cd backend && mvn clean install

# Run all tests with coverage report
mvn clean test jacoco:report

# Run specific test class
mvn test -Dtest=PollServiceTest

# Run all controller tests
mvn test -Dtest=*Controller*

# View coverage report (opens browser)
open target/site/jacoco/index.html
```

### Frontend Tests

```bash
# Install testing dependencies
cd frontend && npm install --save-dev vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event jsdom @vitest/ui @vitest/coverage-v8 msw

# Run tests in watch mode
npm test

# Run all tests once
npm run test:run

# View interactive test UI
npm run test:ui

# Generate coverage report
npm run test:coverage

# View coverage report (opens browser)
open coverage/index.html
```

---

## 📊 Expected Results

### Backend
- **Test Files:** 6
- **Test Cases:** 81+
- **Coverage Target:** 95%+
- **Execution Time:** ~15-20 seconds

### Frontend
- **Test Files:** 5
- **Test Cases:** 60+
- **Coverage Target:** 90%+
- **Execution Time:** ~10-15 seconds

---

## ✅ Verification Steps

### Step 1: Backend Tests (5 mins)
```bash
cd backend
mvn clean install
mvn clean test jacoco:report
# Check: target/site/jacoco/index.html shows 95%+ coverage
```

### Step 2: Frontend Tests (5 mins)
```bash
cd frontend
npm install --save-dev vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event jsdom @vitest/ui @vitest/coverage-v8 msw
npm run test:run
npm run test:coverage
# Check: coverage/index.html shows 90%+ coverage
```

### Step 3: Success Criteria
- ✅ All backend tests pass
- ✅ All frontend tests pass
- ✅ Backend coverage ≥ 95%
- ✅ Frontend coverage ≥ 90%
- ✅ No compilation errors
- ✅ No test warnings

---

## 🔍 Troubleshooting

### Backend Issues

**"Tests not found"**
```bash
# Verify files exist
ls -la backend/src/test/java/com/civiclens/*/
# Check for *Test.java suffix
```

**"Maven build fails"**
```bash
# Clean build
mvn clean install -U

# Force JDK 21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn clean install
```

**"Coverage report not generated"**
```bash
# Verify JaCoCo plugin
grep -n "jacoco" backend/pom.xml

# Regenerate
mvn clean test jacoco:report
```

### Frontend Issues

**"Tests not found"**
```bash
# Check vitest config
grep -A 5 "include:" frontend/vitest.config.js

# Verify test files
find frontend/src/__tests__ -name "*.test.jsx"
```

**"Module not found errors"**
```bash
# Clean node_modules
rm -rf frontend/node_modules frontend/package-lock.json
npm install
npm test
```

**"React Testing Library not working"**
```bash
# Check setup file
cat frontend/vitest.setup.js

# Reinstall testing library
npm install --save-dev @testing-library/react @testing-library/jest-dom
```

---

## 📈 Coverage Goals

| Metric | Backend | Frontend |
|--------|---------|----------|
| Lines | 95% | 90% |
| Branches | 95% | 90% |
| Functions | 95% | 90% |
| Statements | 95% | 90% |

---

## 📝 Key Test Files

**Backend Location:** `backend/src/test/java/com/civiclens/`
- `security/JwtTokenProviderTest.java`
- `service/PollServiceTest.java`
- `service/AspirantServiceTest.java`
- `controller/PollControllerTest.java`
- `controller/AspirantAdminControllerTest.java`
- `repository/PollVoteRepositoryTest.java`
- `integration/AspirantIntegrationTest.java`

**Frontend Location:** `frontend/src/__tests__/`
- `pages/LoginPage.test.jsx`
- `pages/Dashboard.test.jsx`
- `pages/AspirantManagement.test.jsx`
- `api/apiClient.test.js`
- `integration/auth-flow.test.jsx`

---

## 🎯 Next Action

1. Run backend tests: `mvn clean test jacoco:report`
2. Run frontend tests: `npm test`
3. Review coverage reports
4. Document any gaps
5. Proceed to security fixes after verification
