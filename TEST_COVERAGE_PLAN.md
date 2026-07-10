# Civic Lens - 100% Test Coverage Plan

## Overview
This document outlines the comprehensive test strategy for 100% code coverage across backend and frontend.

---

## BACKEND TEST SUITE

### 1. Security Tests (`src/test/java/com/civiclens/security/`)
- **JwtTokenProviderTest**: Token generation, validation, expiration, claim extraction
- **JwtAuthenticationFilterTest**: Token extraction, user authentication, error handling
- **SecurityConfigTest**: CORS config, authorization rules, filter chain

### 2. Service Layer Tests (`src/test/java/com/civiclens/service/`)
- **UserServiceTest** (when created)
  - User registration, validation, profile updates
  - Password management
  
- **AuthServiceTest** (when created)
  - Login success/failure scenarios
  - Token generation and refresh
  
- **PollServiceTest**
  - Vote casting, validation, duplicate prevention
  - Poll results calculation with vote percentages
  - User vote retrieval
  - Candidate filtering by position and location scope
  
- **AspirantServiceTest**
  - Aspirant CRUD operations
  - Email uniqueness validation
  - Location-based filtering
  - Position-based scoping
  
- **CommentServiceTest** (when created)
  - Comment creation, updates, deletion
  - Comment hiding/moderation
  - Nested comments (parent-child relationships)
  - Rating aggregation
  
- **LeaderServiceTest** (when created)
  - Leader creation, updates
  - Position and location filtering
  - Engagement metrics

### 3. Controller Tests (`src/test/java/com/civiclens/controller/`)
- **AuthControllerTest**
  - Login endpoint: valid/invalid credentials, missing fields
  - Signup endpoint: duplicate email, validation errors
  - Token validation
  - Password change
  
- **PollControllerTest**
  - Vote casting: success, duplicate votes, invalid candidates
  - Poll results retrieval with pagination
  - User vote status checking
  - Candidate list retrieval
  
- **CommentControllerTest**
  - Comment creation: valid/invalid input, auth
  - Comment retrieval with pagination
  - Comment hiding/moderation
  - Nested comment handling
  
- **AspirantAdminControllerTest**
  - Create aspirant: success, validation errors, location validation
  - Update aspirant: auth, duplicate email, location changes
  - Delete aspirant: cascade handling
  - List all aspirants
  
- **LeaderControllerTest**
  - Leader retrieval by position, county, constituency, ward
  - Leader detail view
  - Engagement endpoints
  
- **LocationsControllerTest**
  - County listing
  - Constituency filtering by county
  - Ward filtering by constituency
  - Hierarchical validation
  
- **HealthControllerTest**
  - Health check endpoint

### 4. Repository Tests (`src/test/java/com/civiclens/repository/`)
- **UserRepositoryTest**
  - findByEmail: existing/non-existing
  - existsByEmail validation
  
- **PollVoteRepositoryTest**
  - Vote tracking and duplicate detection
  - Vote count aggregation by position/location
  - User vote retrieval with location scope
  
- **CommentRepositoryTest**
  - Comment filtering by leader and visibility
  - Nested comment queries
  - Pagination
  
- **AspirantRepositoryTest**
  - Position and location-based queries
  - Email uniqueness
  
- **LeaderRepositoryTest**
  - Position-based queries
  - Location hierarchical queries

### 5. Entity Tests (`src/test/java/com/civiclens/entity/`)
- Verify entity annotations, relationships, constraints

### 6. Integration Tests (`src/test/java/com/civiclens/integration/`)
- **AuthIntegrationTest**: Full signup → login → token flow
- **PollIntegrationTest**: Vote casting → results display → user vote retrieval
- **CommentIntegrationTest**: Comment creation → moderation → display
- **AspirантIntegrationTest**: Aspirant creation → location validation → display in polls

---

## FRONTEND TEST SUITE

### 1. Component Tests (`src/__tests__/components/`)
- **SignupForm.test.jsx**
  - Form submission with valid/invalid data
  - Email validation
  - Phone number validation
  - Password strength
  - Error message display
  - Loading states
  
- **LocationSelector.test.jsx**
  - County selection
  - Constituency loading based on county
  - Ward loading based on constituency
  - Disabled state handling
  - Error handling
  
- **Navbar.test.jsx**
  - Navigation links display
  - User menu
  - Logout functionality
  - Responsive behavior

### 2. Page Tests (`src/__tests__/pages/`)
- **LoginPage.test.jsx**
  - Form submission
  - Error handling
  - Redirect on success
  - "Forgot password" link
  
- **SignupPage.test.jsx**
  - Multi-step signup flow
  - Form validation at each step
  - Location selection integration
  - Success redirect
  
- **Dashboard.test.jsx**
  - Leader list display
  - Candidate comparison sections
  - Vote submission and validation
  - Vote state management
  - One-vote-per-position enforcement
  - Loading states
  
- **LeaderPage.test.jsx**
  - Leader detail rendering
  - Comment display and threading
  - Rating calculation
  - Comment submission
  
- **AspirantManagement.test.jsx**
  - Aspirant list display
  - Create aspirant form
  - Update aspirant flow
  - Delete aspirant confirmation
  - Location-based form state
  - Error message display
  
- **CommentModeration.test.jsx**
  - Comment list with filters
  - Hide/unhide functionality
  - Delete confirmation
  - Pagination
  
- **CompleteProfile.test.jsx**
  - Profile form submission
  - Location update
  - Success/error handling
  
- **UpdatePassword.test.jsx**
  - Current password validation
  - New password confirmation
  - Error handling

### 3. API Client Tests (`src/__tests__/api/`)
- **apiClient.test.js**
  - Auth endpoints: login, signup, logout, checkToken
  - Poll endpoints: getCandidates, castVote, getPollResults
  - Comment endpoints: getComments, postComment, hideComment
  - Leader endpoints: getLeadersByPosition, getLeaderDetail
  - Aspirant endpoints: getAllAspirants, createAspirant, updateAspirant, deleteAspirant
  - Error handling and interceptors
  - Token management

### 4. Utility Tests (`src/__tests__/utils/`)
- Form validation functions
- Data formatting utilities
- Local storage handling
- URL parameter parsing

### 5. Integration Tests (`src/__tests__/integration/`)
- **Auth Flow**: Signup → Login → Dashboard
- **Voting Flow**: View candidates → Submit vote → Update results
- **Comment Flow**: View leader → Post comment → See moderation
- **Admin Flow**: Create aspirant → Update aspirant → Delete aspirant

---

## Test Tools & Dependencies

### Backend
- **JUnit 5** (Jupiter)
- **Mockito** for mocking
- **Spring Boot Test** for integration tests
- **Testcontainers** for PostgreSQL testing
- **RestAssured** for API endpoint testing

### Frontend
- **Vitest** or **Jest** as test runner
- **React Testing Library** for component testing
- **MSW** (Mock Service Worker) for API mocking
- **@testing-library/user-event** for user interactions

---

## Coverage Goals by Component

| Component | Type | Target Coverage | Priority |
|-----------|------|-----------------|----------|
| AuthService/Controller | Business Logic | 100% | CRITICAL |
| PollService/Controller | Business Logic | 100% | CRITICAL |
| AspirantService/Controller | Business Logic | 100% | CRITICAL |
| SecurityConfig | Security | 100% | CRITICAL |
| JwtTokenProvider | Security | 100% | CRITICAL |
| CommentService/Controller | Feature | 95% | HIGH |
| LeaderService/Controller | Feature | 95% | HIGH |
| Repositories | Data Access | 90% | MEDIUM |
| Dashboard | UI | 90% | HIGH |
| LoginPage | UI | 90% | CRITICAL |
| AspirantManagement | UI | 85% | MEDIUM |
| Utilities | Helper | 95% | MEDIUM |

---

## Test Execution Plan

### Phase 1: Backend Unit Tests (Week 1)
1. Security tests (JwtTokenProvider, SecurityConfig)
2. Service layer tests (PollService, AspirantService)
3. Repository tests

### Phase 2: Backend Integration Tests (Week 1-2)
1. Controller tests with MockMvc
2. End-to-end API flow tests
3. Database integration tests

### Phase 3: Frontend Unit Tests (Week 2)
1. Component unit tests
2. API client tests
3. Utility function tests

### Phase 4: Frontend Integration Tests (Week 2-3)
1. Page flow tests
2. User journey tests
3. Error scenario tests

### Phase 5: Coverage Reporting (Week 3)
1. Generate coverage reports
2. Identify gaps
3. Close remaining coverage gaps

---

## Acceptance Criteria

✅ Overall code coverage ≥ 95%
✅ All critical paths tested
✅ All error scenarios covered
✅ All edge cases identified and tested
✅ Security tests for auth and data access
✅ Integration tests for main workflows
✅ No hardcoded test data (fixtures/factories used)

---

## Maintenance

- Run full test suite on every commit
- Update tests when features are added
- Maintain coverage reports monthly
- Review and refactor test code quarterly
