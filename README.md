# CRMBuild Service

Backend service for CRMBuild - The Agentic CRM Platform

## Overview

CRMBuild is a comprehensive CRM solution that unifies sales operations, automates processes, and helps close more deals through intelligent sales operations.

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: MongoDB
- **Authentication**: Keycloak (OAuth2/JWT)
- **Security**: Spring Security with OAuth2 Resource Server
- **Object Mapping**: ModelMapper
- **Build Tool**: Maven

## Architecture

### Components

1. **Keycloak**: Identity and Access Management
   - Handles authentication and user management
   - Issues JWT tokens
   - Manages user credentials securely

2. **MongoDB**: NoSQL database for user data
   - Stores user profiles and application data
   - Synced with Keycloak users

3. **Spring Boot**: Backend API
   - Validates JWT tokens from Keycloak
   - Manages business logic
   - Integrates Keycloak and MongoDB

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker Desktop (for Keycloak and MongoDB)
- Keycloak running on port 8080
- MongoDB running on port 27017

## Getting Started

### 1. Start Required Services

#### Start Keycloak
```bash
docker run -d --name keycloak \
  -p 8080:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest \
  start-dev
```

#### Start MongoDB
```bash
docker run -d --name mongodb -p 27017:27017 mongo:latest
```

### 2. Configure Keycloak

1. Access Keycloak Admin Console: http://localhost:8080
2. Login with `admin` / `admin`
3. Create a realm (e.g., `lahiru`)
4. Create a client (e.g., `lahiru`)
5. Configure client settings:
   - Access Type: Confidential
   - Valid Redirect URIs: `/*`
6. Get the Client Secret from the Credentials tab

### 3. Configure Application

Update `src/main/resources/application.yml`:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: your-client-id
            client-secret: your-client-secret
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/your-realm

keycloak:
  realm: your-realm
  resource: your-client-id
  auth-server-url: http://localhost:8080
```

Or use environment variables:
```bash
export KEYCLOAK_CLIENT_SECRET=your_client_secret
export KEYCLOAK_REALM=your_realm
export KEYCLOAK_CLIENT_ID=your_client_id
```

### 4. Build and Run

```bash
# Navigate to project directory
cd "C:\Users\User\Desktop\Lara Boy\backend\crm-be-springboot"

# Clean and package
mvn clean package -DskipTests

# Run the application
java -jar target/crmbuild-service-0.0.1-SNAPSHOT.jar
```

The application will start on port 8083 with context path `/api/v1`.

## API Endpoints

Base URL: `http://localhost:8083/api/v1`

### Authentication Endpoints

#### Register New User
```http
POST /auth/register
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123",
  "firstName": "First",
  "lastName": "Last"
}
```

**Response:**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "username": "newuser",
  "email": "newuser@example.com",
  "firstName": "First",
  "lastName": "Last",
  "role": "USER",
  "enabled": true,
  "createdAt": "2025-10-22T19:08:11.885"
}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "username",
  "password": "password"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 300,
  "username": "username",
  "email": "user@example.com"
}
```

#### Refresh Token
```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "your-refresh-token"
}
```

#### Get Current User Profile
```http
GET /auth/me
Authorization: Bearer {access-token}
```

**Response:**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "username": "username",
  "email": "user@example.com",
  "firstName": "First",
  "lastName": "Last",
  "role": "USER",
  "enabled": true,
  "createdAt": "2025-10-22T19:08:11.885"
}
```

#### Logout
```http
POST /auth/logout
Authorization: Bearer {access-token}
Content-Type: application/json

{
  "refreshToken": "your-refresh-token"
}
```

## Project Structure

```
src/main/java/com/ceedpods/crmbuild/
├── config/
│   ├── SecurityConfig.java          # Spring Security & OAuth2 configuration
│   ├── ModelMapperConfig.java       # ModelMapper bean configuration
│   └── GlobalExceptionHandler.java  # Global error handling
├── constants/
│   └── AppConstants.java            # Application-wide constants
├── controller/
│   └── AuthController.java          # Authentication endpoints
├── dto/
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── AuthResponse.java
│   └── UserResponse.java
├── entity/
│   └── User.java                    # MongoDB user entity
├── enums/
│   ├── UserRole.java
│   ├── LeadSource.java
│   ├── LeadStage.java
│   └── CommunicationType.java
├── repository/
│   └── UserRepository.java          # MongoDB repository
├── service/
│   ├── KeycloakService.java         # Keycloak integration service
│   └── AuthService.java             # Authentication business logic
└── CrmBuildApplication.java         # Main application class
```

## Security

- All endpoints except registration, login, and token refresh require authentication
- Authentication is handled via OAuth2 JWT tokens from Keycloak
- Passwords are never stored in MongoDB - only in Keycloak (hashed)
- CORS is configured for development (localhost:3000, localhost:4200)
- Session management is stateless (no server-side sessions)

## Development

### Building the Project
```bash
mvn clean install
```

### Running Tests
```bash
mvn test
```

### Running in Development Mode
```bash
mvn spring-boot:run
```

## Configuration

Key configuration properties in `application.yml`:

- `server.port`: Application port (default: 8083)
- `server.servlet.context-path`: API base path (default: /api/v1)
- `spring.data.mongodb.uri`: MongoDB connection string
- `spring.security.oauth2.*`: OAuth2 and Keycloak configuration

## Troubleshooting

### Port Already in Use
If the port is already in use, either:
1. Change the port in `application.yml`
2. Kill the process using the port

### Keycloak Connection Error
- Ensure Keycloak is running: `docker ps`
- Verify Keycloak URL is accessible: http://localhost:8080
- Check realm and client configuration

### MongoDB Connection Error
- Ensure MongoDB is running: `docker ps`
- Check MongoDB connection string in `application.yml`
- Verify MongoDB is accessible on port 27017

### Authentication Issues
- Verify client secret is correct in configuration
- Ensure realm name matches in Keycloak and application.yml
- Check JWT token hasn't expired

## Future Features

### Phase 1 (MVP)
- Core User & Access Management (Admin, Manager, Sales Rep roles)
- Basic Lead Management & AI Routing
- Communication & Engagement (VoIP, Email, SMS)
- Performance Monitoring & Dashboards

### Phase 2 (Enhancements)
- Advanced Lead Management with sophisticated AI routing
- Enhanced Communication & AI Intelligence
- Comprehensive Performance & Reporting

## External Integrations (Planned)
- Twilio: SMS, Voice, and WhatsApp communications
- Meta Ads: Lead generation integration
- Google Ads: Lead generation integration
- Deepgram: Call transcription and AI analysis
- Salesforce: Lead import/export

## License

This project is proprietary to CeedPods.
