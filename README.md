# CRMBuild Service

Backend service for CRMBuild - The Agentic CRM Platform

## Overview

CRMBuild is a comprehensive CRM solution that unifies sales operations, automates processes, and helps close more deals through intelligent sales operations.

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: SQL Server (H2 for development)
- **Security**: Spring Security with JWT
- **Build Tool**: Maven
- **Documentation**: OpenAPI/Swagger

## Features

### Phase 1 (MVP)
- Core User & Access Management (Admin, Manager, Sales Rep roles)
- Basic Lead Management & AI Routing
- Communication & Engagement (VoIP, Email, SMS)
- Performance Monitoring & Dashboards

### Phase 2 (Enhancements)
- Advanced Lead Management with sophisticated AI routing
- Enhanced Communication & AI Intelligence
- Comprehensive Performance & Reporting

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- SQL Server (for production) or H2 (for development)

### Running the Application

1. **Development Mode** (with H2 database):
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. **Production Mode** (with SQL Server):
   ```bash
   mvn spring-boot:run
   ```

### Configuration

The application uses environment variables for configuration. Create a `.env` file or set the following variables:

```properties
# Database
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# JWT
JWT_SECRET=your_jwt_secret_key

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email
MAIL_PASSWORD=your_email_password

# Twilio
TWILIO_ACCOUNT_SID=your_twilio_account_sid
TWILIO_AUTH_TOKEN=your_twilio_auth_token
TWILIO_PHONE_NUMBER=your_twilio_phone_number

# Meta Ads
META_APP_ID=your_meta_app_id
META_APP_SECRET=your_meta_app_secret
META_ACCESS_TOKEN=your_meta_access_token

# Google Ads
GOOGLE_ADS_CLIENT_ID=your_google_ads_client_id
GOOGLE_ADS_CLIENT_SECRET=your_google_ads_client_secret
GOOGLE_ADS_DEVELOPER_TOKEN=your_google_ads_developer_token

# Deepgram
DEEPGRAM_API_KEY=your_deepgram_api_key
```

## API Documentation

Once the application is running, you can access the API documentation at:
- Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`
- OpenAPI Spec: `http://localhost:8080/api/v1/v3/api-docs`

## Project Structure

```
src/main/java/com/ceedpods/crmbuild/
├── config/              # Configuration classes
├── controller/          # REST controllers
│   ├── user/           # User management endpoints
│   ├── lead/           # Lead management endpoints
│   ├── communication/  # Communication endpoints
│   └── performance/    # Performance and reporting endpoints
├── dto/                # Data Transfer Objects
├── entity/             # JPA entities
│   ├── user/          # User-related entities
│   ├── lead/          # Lead-related entities
│   ├── communication/ # Communication entities
│   └── performance/   # Performance entities
├── enums/              # Enumeration classes
├── exception/          # Custom exceptions
├── mapper/             # MapStruct mappers
├── repository/         # JPA repositories
├── security/           # Security configuration
├── service/            # Business logic
│   ├── user/          # User services
│   ├── lead/          # Lead services
│   ├── communication/ # Communication services
│   ├── performance/   # Performance services
│   └── external/      # External API integrations
└── util/               # Utility classes
```

## External Integrations

- **Twilio**: SMS, Voice, and WhatsApp communications
- **Meta Ads**: Lead generation integration
- **Google Ads**: Lead generation integration
- **Deepgram**: Call transcription and AI analysis
- **Salesforce**: Lead import/export

## Development

### Building the Project

```bash
mvn clean install
```

### Running Tests

```bash
mvn test
```

### Code Style

The project follows standard Java coding conventions and uses MapStruct for object mapping.

## Deployment

The application is designed to be deployed on Azure cloud platform with:
- Azure SQL Database
- Azure App Service
- Azure Application Insights for monitoring

## Contributing

1. Create a feature branch
2. Make your changes
3. Write tests
4. Submit a pull request

## License

This project is proprietary to CeedPods.