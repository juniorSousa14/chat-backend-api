package br.com.juniorjvsousa.chat_service.domain.service.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new HashMap<>();

        // Pega cada campo que deu erro e a mensagem
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            erros.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("status", HttpStatus.BAD_REQUEST.value());
        resposta.put("erro", "Erro de Validação");
        resposta.put("detalhes", erros);
        resposta.put("timestamp", LocalDateTime.now());

        return ResponseEntity.badRequest().body(resposta);
    }

    //  Trata erros de Regra de Negócio (ResponseStatusException)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessErrors(ResponseStatusException ex) {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("status", ex.getStatusCode().value());
        resposta.put("erro", ex.getReason());
        resposta.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(ex.getStatusCode()).body(resposta);
    }

}