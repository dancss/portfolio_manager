package com.portfolio.manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
// trigger recodificação
@Configuration
@Component
@EnableWebSecurity
public class SecurityConfig {

    @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // 1. Desabilita o CSRF para permitir POST/PUT/DELETE sem precisar de tokens
        .csrf(csrf -> csrf.disable()) 
        
        // 2. Define as regras de autorização de requisições
        .authorizeHttpRequests(auth -> auth
            // Libera o Swagger UI e a documentação JSON
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            
            // Libera temporariamente todos os seus endpoints de API para os testes
            .requestMatchers("/api/**").permitAll() 
            
            // Qualquer outra requisição precisará de autenticação
            .anyRequest().authenticated()
        );

    return http.build();
}
}