package br.com.globalti.prova.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Essa classe serve para receber email e senha na hora do login.
 */
public record LoginRequest(
        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String senha
) {
}
