package br.com.juniorjvsousa.chat_service.domain.service.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Chat Service API")
                        .description("API de chat em tempo real com Spring Boot")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Junior Sousa")
                                .email("junior@teste.com")
                        ));
    }
}
