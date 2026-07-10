# Civic Lens - Spring Boot Backend

This is the Spring Boot backend for the Civic Lens application, replacing Supabase with a local H2 database.

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Node.js and npm (for the React frontend)

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/civiclens/
│   │   │   ├── controller/          # REST API controllers
│   │   │   ├── service/             # Business logic services
│   │   │   ├── repository/          # Data access layer
│   │   │   ├── entity/              # JPA entities
│   │   │   ├── config/              # Spring configuration
│   │   │   ├── security/            # JWT and security
│   │   │   ├── dto/                 # Data transfer objects
│   │   │   └── CivicLensApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-dev.properties
│   └── test/
├── pom.xml
└── README.md
```

## Setup & Installation

### 1. Build the Backend

```bash
cd backend
mvn clean install
```

### 2. Run the Backend

```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### 3. Access H2 Console (for development)

While the application is running, you can access the H2 database console at:
```
http://localhost:8080/h2-console
```

- **JDBC URL**: `jdbc:h2:mem:civiclensdb`
- **Username**: `sa`
- **Password**: (leave blank)

## Database Schema

The backend automatically creates the following tables on startup:

### profiles
- id (UUID, Primary Key)
- email (String, Unique)
- password_hash (String)
- name (String)
- phone (String)
- national_id (String)
- is_leader (Boolean)
- is_aspirant (Boolean)
- is_admin (Boolean)
- position (String) - President, Senator, MP, etc.
- county_id (UUID, Foreign Key)
- constituency_id (UUID, Foreign Key)
- ward_id (UUID, Foreign Key)
- created_at (Timestamp)
- updated_at (Timestamp)

### leaders
- id (UUID, Primary Key)
- name (String)
- position (String)
- bio (String, Text)
- image_url (String)
- county_id (UUID, Foreign Key)
- constituency_id (UUID, Foreign Key)
- ward_id (UUID, Foreign Key)
- created_at (Timestamp)
- updated_at (Timestamp)

### counties
- id (UUID, Primary Key)
- name (String, Unique)
- code (String)

### constituencies
- id (UUID, Primary Key)
- name (String)
- code (String)
- county_id (UUID, Foreign Key)

### wards
- id (UUID, Primary Key)
- name (String)
- code (String)
- constituency_id (UUID, Foreign Key)

### ratings
- id (UUID, Primary Key)
- user_id (UUID, Foreign Key)
- leader_id (UUID, Foreign Key)
- rating (Integer) - 1-5
- created_at (Timestamp)
- updated_at (Timestamp)

### comments
- id (UUID, Primary Key)
- user_id (UUID, Foreign Key)
- leader_id (UUID, Foreign Key)
- text (String, Text)
- created_at (Timestamp)
- updated_at (Timestamp)

## API Endpoints

### Authentication
- `POST /api/auth/signup` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `POST /api/auth/forgot-password` - Request password reset
- `POST /api/auth/reset-password` - Reset password with token
- `GET /api/auth/me` - Get current user

### Profiles
- `GET /api/profiles/{userId}` - Get user profile
- `PUT /api/profiles/{userId}` - Update user profile
- `PUT /api/profiles/{userId}/complete` - Complete profile after signup

### Leaders
- `GET /api/leaders` - Get all leaders
- `GET /api/leaders/{id}` - Get leader by ID
- `GET /api/leaders/position/{position}` - Get leaders by position
- `GET /api/leaders/county/{countyId}` - Get leaders by county
- `GET /api/leaders/constituency/{constituencyId}` - Get leaders by constituency
- `POST /api/leaders/{leaderId}/rate` - Rate a leader

### Locations
- `GET /api/locations/counties` - Get all counties
- `GET /api/locations/constituencies/{countyId}` - Get constituencies by county
- `GET /api/locations/wards/{constituencyId}` - Get wards by constituency
- `GET /api/locations/hierarchy/{countyId}` - Get full location hierarchy

### Ratings
- `GET /api/ratings/leader/{leaderId}` - Get ratings for a leader
- `POST /api/ratings` - Create a rating
- `PUT /api/ratings/{ratingId}` - Update a rating

### Comments
- `GET /api/comments/leader/{leaderId}` - Get comments for a leader
- `POST /api/comments` - Create a comment
- `DELETE /api/comments/{commentId}` - Delete a comment

### Admin
- `GET /api/admin/stats` - Get dashboard statistics
- `GET /api/admin/users` - Get all users
- `GET /api/admin/leaders` - Get all leaders (admin view)
- `PUT /api/admin/leaders/{leaderId}` - Update leader
- `DELETE /api/admin/leaders/{leaderId}` - Delete leader

## Configuration

Edit `src/main/resources/application.properties` to customize:

```properties
# Server
server.port=8080
server.servlet.context-path=/api

# H2 Database
spring.datasource.url=jdbc:h2:mem:civiclensdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true

# JWT
jwt.secret=your-secret-key-change-this-in-production
jwt.expiration=86400000

# CORS
cors.allowedOrigins=http://localhost:3000,http://localhost:5173
```

## Running Backend and Frontend Together

### Terminal 1: Start Backend
```bash
cd backend
mvn spring-boot:run
```

### Terminal 2: Start Frontend
```bash
cd frontend
npm install
npm run dev
```

Frontend will be available at `http://localhost:5173` (or configured Vite port)

## Security Notes

⚠️ **Important for Production**:
1. Change the JWT secret in `application.properties`
2. Move from H2 to PostgreSQL/MySQL for production
3. Enable HTTPS
4. Implement proper CORS policies
5. Add rate limiting
6. Use environment variables for sensitive data

## Migration from Supabase

The React frontend has been updated to use the new Spring Boot backend:

1. **Removed** Supabase dependencies from `package.json`
2. **Added** Axios for HTTP requests
3. **Created** `src/api/apiClient.js` with all API endpoints
4. **Updated** all components to use the new API client
5. **Implemented** JWT token storage in localStorage

### Environment Variables (Frontend)

Create `.env` in the root directory:
```
VITE_API_URL=http://localhost:8080/api
```

## Development Tips

- Use Postman or VS Code REST Client to test API endpoints
- Check H2 console for database state during development
- JWT tokens are stored in localStorage for persistence
- Tokens expire after 24 hours (configurable in properties)

## Troubleshooting

### Port 8080 Already in Use
```bash
# Find process using port 8080
lsof -i :8080
# Kill process
kill -9 <PID>
```

### H2 Console Connection Issues
- Ensure the backend is running
- Check that `spring.h2.console.enabled=true` in properties
- Clear browser cache and try again

### CORS Errors
- Verify `cors.allowedOrigins` in `application.properties`
- Ensure frontend is accessing the correct backend URL

## Next Steps

1. Set up database persistence (replace H2 with PostgreSQL)
2. Implement email verification for signup
3. Add OAuth integration (Google, GitHub)
4. Set up logging and monitoring
5. Implement API rate limiting
6. Add comprehensive error handling
7. Create API documentation (Swagger/OpenAPI)

## Support

For issues or questions, refer to:
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Spring Security: https://spring.io/projects/spring-security
- H2 Database: https://www.h2database.com/
