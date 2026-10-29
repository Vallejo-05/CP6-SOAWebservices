package br.com.fiap.incidentmanagement.infra.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Swagger UI: http://localhost:8090/swagger-ui.html
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(
                        "bearer-key",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtido em POST /login")))
                .info(new Info()
                        .title("Incident Management")
                        .version("v1")
                        .description("API REST para registro e acompanhamento de ocorrências escolares. " +
                                "Perfis: PROFESSOR, ANALISTA, COORDENADOR e ADMINISTRATIVO."));
    }
}
