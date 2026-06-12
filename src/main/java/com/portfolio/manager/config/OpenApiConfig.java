package com.portfolio.manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Portfolio Manager API")
                        .version("1.0.0")
                        .description("Sistema de gestão de portfólio de projetos com regras estritas de alocação de membros, " +
                                     "cálculo dinâmico de risco e relatórios consolidados de PMO.")
                        .contact(new Contact()
                                .name("Daniel - Equipe de Desenvolvimento")
                                .email("suporte@portfolio.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}