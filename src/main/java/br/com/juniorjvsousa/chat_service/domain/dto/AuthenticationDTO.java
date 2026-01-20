package br.com.juniorjvsousa.chat_service.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationDTO(
        @NotBlank @Email String email,
        @NotBlank String senha
) {
}
