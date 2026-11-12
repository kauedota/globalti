package br.com.globalti.prova.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Essa classe serve para receber os dados do cadastro público.
 * Aqui não existe campo "role": quem se registra sempre vira USER.
 */
public record RegisterRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome pode ter no máximo 120 caracteres")
        String nome,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 180, message = "O email pode ter no máximo 180 caracteres")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String senha
) {
}
