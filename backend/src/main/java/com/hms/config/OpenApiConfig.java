package com.hms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Custom OpenAPI Configuration for Swagger UI.
 * Enables Bearer token JWT Authorization directly in the Swagger interface.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Bearer Auth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Hospital Management System (HMS) API")
                        .version("1.0.0")
                        .description("Comprehensive REST API for doctor onboarding, appointment scheduling, prescription tracking, and payment processing.")
                        .license(new License().name("HMS Internal License")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token in the format: <token>")));
    }
}
