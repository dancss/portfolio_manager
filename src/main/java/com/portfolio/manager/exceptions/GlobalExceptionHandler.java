package com.portfolio.manager.exceptions;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Tratamento para validações do @Valid (Campos obrigatórios, formatos, etc.)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Erro de Validação");

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Tratamento para argumentos inválidos ou regras de negócio violadas (Regra de alocação de membros, etc.)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Requisição Inválida");
        body.put("message", ex.getMessage());
        body.put("errors", null);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Tratamento para estados inválidos (Limite de membros por projeto, etc.)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Estado de Recurso Inválido");
        body.put("message", ex.getMessage());
        body.put("errors", null);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * PASSO 1: Tratamento genérico para erros inesperados do sistema (Erro 500)
     * Modificado para não mascarar as falhas internas geradas na rota do Swagger.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex, HttpServletRequest request) {
        
        // Verifica se o erro veio da tentativa de carregar o Swagger ou os documentos da API
        if (request.getRequestURI().contains("/v3/api-docs") || request.getRequestURI().contains("/swagger-ui")) {
            System.err.println("=== EXCEÇÃO INTERNA DO SWAGGER DETECTADA NO GLOBAL EXCEPTION ===");
            ex.printStackTrace(); // Cospe a árvore de erros completa (stacktrace) em vermelho no terminal
            throw new RuntimeException("Falha na varredura do Swagger: ", ex);
        }

        // Fluxo normal do sistema para outros endpoints (Mascarando erro 500 para o cliente de forma amigável)
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Erro Interno do Servidor");
        body.put("message", "Ocorreu um erro inesperado no sistema. Por favor, tente novamente mais tarde.");
        body.put("path", request.getRequestURI());
        body.put("errors", null);

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}