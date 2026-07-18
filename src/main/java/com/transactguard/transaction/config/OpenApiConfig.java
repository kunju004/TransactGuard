package com.transactguard.transaction.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI riskLaneOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TransactGuard API")
                        .version("0.0.1")
                        .description("Real-time payment risk scoring and transaction authorization APIs.")
                        .contact(new Contact()
                                .name("TransactGuard Engineering"))
                        .license(new License()
                                .name("Portfolio Project")))
                .servers(List.of(new Server()
                        .url("http://localhost:8080")
                        .description("Local development")));
    }
}
