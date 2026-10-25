package com.aditya.lms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiDocConfig {

    /**
     * Bean to provide a custom OpenAPI instance
     */
    @Bean
    public OpenAPI customOpenAPI() {
        /* scheme name is case-sensitive */
        final String schemeName = "BearerAuth";
        final List<Server> servers = List.of(
                new Server().url("http://localhost:8081").description("Local")
        );

        return new OpenAPI()
                .servers(servers)
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components()
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

}
