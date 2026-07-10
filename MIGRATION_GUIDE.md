# Migration Guide: Supabase to Spring Boot

## Overview

The Civic Lens application has been successfully migrated from Supabase to a Spring Boot backend with H2 database. This guide explains the changes and how to set up and run the application.

## What Changed

### Backend
- **Before**: Supabase (cloud-based PostgreSQL)
- **After**: Spring Boot with H2 (local in-memory database)

### Frontend
- **Removed**: `@supabase/supabase-js` dependency
- **Added**: `axios` for HTTP requests
- **Added**: `src/api/apiClient.js` with all API endpoints
- **Updated**: All pages and components to use new API client

## Project Structure

```
Civic-Lens/
├── frontend (React)
│   ├── src/
│   │   ├── api/
│   │   │   └── apiClient.js          # New: API client using axios
│   │   ├── components/
│   │   ├── pages/
│   │   ├── App.jsx                   # Updated: Uses new auth flow
│   │   └── main.jsx
│   ├── package.json                  # Updated: Removed Supabase, added Axios
│   └── vite.config.js
│
└── backend (Spring Boot)
    ├── src/
    │   ├── main/java/com/civiclens/
    │   │   ├── controller/            # REST API endpoints
    │   │   ├── service/               # Business logic
    │   │   ├── repository/            # Database layer
    │   │   ├── entity/                # JPA entities
    │   │   ├── config/                # Configuration
    │   │   ├── security/              # JWT & security
    │   │   └── dto/                   # Data transfer objects
    │   └── resources/
    │       └── application.properties
    ├── pom.xml                        # Maven dependencies
    └── README.md
```

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Node.js 16+
- npm or yarn

### Step 1: Start Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend runs on: `http://localhost:8080`

### Step 2: Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on: `http://localhost:5173`

### Step 3: Access Application

Open browser and go to `http://localhost:5173`

## API Integration Changes

### Before (Supabase)
```javascript
import supabase from './supabaseClient';

// Authentication
const { data: { user } } = await supabase.auth.getUser();
await supabase.auth.signUp({ email, password });

// Database queries
const { data, error } = await supabase
  .from('profiles')
  .select('*')
  .eq('id', userId)
  .single();
```

### After (Spring Boot)
```javascript
import { authAPI, profileAPI } from './api/apiClient';

// Authentication
const { data: user } = await authAPI.getCurrentUser();
await authAPI.signup(email, password, name);

// API calls
const { data: profile } = await profileAPI.getProfile(userId);
```

## Data Migration

### User Profiles
- User credentials moved from Supabase Auth to `profiles` table
- Passwords hashed using bcrypt before storage
- JWT tokens used for session management

### Leaders & Locations
- Migrated to relational tables: `leaders`, `counties`, `constituencies`, `wards`
- Proper foreign keys and relationships established
- H2 database auto-creates schema on startup

### Ratings & Comments
- Stored in dedicated tables: `ratings`, `comments`
- User and leader references via foreign keys

## Authentication Flow

### New Auth Flow (JWT-based)

1. **Login**
   - User submits email/password
   - Backend validates and returns JWT token
   - Frontend stores token in localStorage

2. **Protected Requests**
   - Frontend includes token in Authorization header
   - Backend validates token using JWT filter
   - Token expires after 24 hours

3. **Logout**
   - Frontend removes token from localStorage
   - Backend invalidates session

## Key Components Updated

### `src/App.jsx`
- Replaced `supabase.auth.getUser()` with `authAPI.getCurrentUser()`
- Updated redirect logic for new auth flow

### `src/api/apiClient.js` (New)
- Axios instance with base URL configuration
- Request/response interceptors for JWT handling
- All API endpoint definitions

### All Page Components
- Updated to use API client instead of Supabase
- Changed data fetching from real-time to REST endpoints
- Updated state management for new data structure

## Database Console Access

While backend is running:
- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:civiclensdb`
- **Username**: `sa`
- **Password**: (blank)

## Environment Variables

### Frontend `.env`
```
VITE_API_URL=http://localhost:8080/api
```

### Backend `application.properties`
```properties
jwt.secret=your-secret-key
jwt.expiration=86400000
cors.allowedOrigins=http://localhost:5173
```

## Troubleshooting

### Issue: 401 Unauthorized Errors
**Solution**: Ensure JWT token is stored in localStorage and included in requests

### Issue: CORS Errors
**Solution**: Check `cors.allowedOrigins` in backend `application.properties`

### Issue: Database Connection Failed
**Solution**: Verify backend is running and H2 is properly configured

### Issue: "Cannot find module" errors
**Solution**: Run `npm install` in frontend directory

## Performance Comparison

| Aspect | Supabase | Spring Boot + H2 |
|--------|----------|-----------------|
| Setup Time | Minutes (cloud) | Seconds (local) |
| Database | Cloud PostgreSQL | Local H2 |
| Real-time | Built-in | Requires polling/WebSocket |
| Cost | Paid tier | Free (local dev) |
| Deployment | Cloud | Any server |
| Development | Remote | Fast local iteration |

## Next Steps

1. **Testing**: Run full test suite
2. **Performance**: Monitor database queries
3. **Production**: Consider PostgreSQL for production
4. **Security**: Update JWT secret for production
5. **Documentation**: API documentation using Swagger

## Important Notes

- ⚠️ H2 is in-memory by default (data lost on restart)
- For persistence: Enable H2 file mode in `application.properties`
- For production: Migrate to PostgreSQL or MySQL
- JWT secret should be stored in environment variables for production

## Support & Debugging

### Enable Detailed Logging
In `application.properties`:
```properties
logging.level.root=INFO
logging.level.com.civiclens=DEBUG
logging.level.org.springframework.security=DEBUG
```

### View SQL Queries
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### Test API Endpoints
Use Postman collection (to be created) or VS Code REST Client with `.http` files

## Migration Checklist

- [x] Create Spring Boot backend project
- [x] Set up JPA entities for all tables
- [x] Implement REST controllers
- [x] Configure JWT authentication
- [x] Set up H2 database
- [x] Create API client for React
- [x] Update all React components
- [x] Remove Supabase dependencies
- [x] Test authentication flow
- [x] Test data operations
- [ ] Performance testing
- [ ] Production deployment setup
- [ ] Database persistence configuration

## Questions?

Refer to:
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [Backend Setup Guide](./BACKEND_SETUP.md)
