package br.com.juniorjvsousa.chat_service.domain.service.infra.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 1. Configura o "tipo" de segurança (o cadeado)
                .components(new Components()
                        .addSecuritySchemes("bearer-key",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                // 2. AQUI ESTÁ A CORREÇÃO: Diz para o Swagger USAR essa segurança em todas as rotas
                .addSecurityItem(new SecurityRequirement().addList("bearer-key"))

                // 3. Informações do projeto
                .info(new Info()
                        .title("Chat Service API")
                        .description("API do Chat Service")
                        .version("1.0.0"));
    }
}
