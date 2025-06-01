package org.bydefault.smartclinic.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerDocumentationConfig {

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().addSecurityItem(new SecurityRequirement().
                        addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes
                        ("Bearer Authentication", createAPIKeyScheme()))
                .info(new Info().title("Smart Clinic REST API")
                        .description("REST API for Smart Clinic management system providing endpoints for patient management, appointments, and medical records.")
                        .version("1.0.0").contact(new Contact().name("Ndifoin Hilary Nkaise")
                                .email("ndifoinhilary@gmail.com")
                                .url("https://www.smartclinic.com"))
                        .license(new License().name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
