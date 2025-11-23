package com.ceedpods.crmbuild.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) configuration for CRMBuild API documentation
 */
@Configuration
public class OpenAPIConfiguration {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        // Define the Bearer JWT authentication scheme
        SecurityScheme jwtSecurityScheme = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization")
            .description("JWT authentication token. Obtain from /auth/login endpoint.");

        return new OpenAPI()
            .info(new Info()
                .title("CRMBuild API")
                .version("1.2.0")
                .description("""
                    ## CRMBuild - Agentic CRM Platform API

                    This is the REST API documentation for CRMBuild, a comprehensive Customer Relationship Management platform.

                    ### Features
                    - **Authentication**: JWT-based authentication with refresh tokens
                    - **User Management**: Role-based access control (Admin, Manager, Sales Rep)
                    - **Lead Management**: Track and manage sales leads
                    - **Deal Management**: Manage sales opportunities and deals
                    - **Product Management**: Catalog and manage products
                    - **Messaging**: Integrated Email, SMS, WhatsApp, and Voice communications
                    - **Permissions**: Granular permission system for access control

                    ### Authentication
                    Most endpoints require authentication via JWT bearer token. To authenticate:
                    1. Use the `/auth/login` endpoint with valid credentials
                    2. Copy the `accessToken` from the response
                    3. Click the "Authorize" button at the top of this page
                    4. Enter: `Bearer <your-access-token>`
                    5. Click "Authorize"

                    ### Error Responses
                    All endpoints return errors in a consistent format:
                    ```json
                    {
                      "success": false,
                      "message": "Error description",
                      "data": null
                    }
                    ```

                    ### Permissions
                    Operations are restricted based on user roles and assigned permissions:
                    - **ADMIN**: Full system access
                    - **MANAGER**: Team management and reporting
                    - **SALES_REP**: Limited access to assigned leads and deals
                    """)
                .contact(new Contact()
                    .name("CRMBuild Support")
                    .email("support@ceedpods.com")
                    .url("https://ceedpods.com"))
                .license(new License()
                    .name("Proprietary")
                    .url("https://ceedpods.com/license")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8084/api/v1")
                    .description("Local Development Server"),
                new Server()
                    .url("https://ceedpodservice.wittycliff-5b88c7b4.westus2.azurecontainerapps.io/api/v1")
                    .description("Production Server")))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", jwtSecurityScheme))
            .addSecurityItem(new SecurityRequirement()
                .addList("Bearer Authentication"));
    }
}
