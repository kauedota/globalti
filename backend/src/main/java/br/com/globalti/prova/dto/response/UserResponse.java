package br.com.globalti.prova.dto.response;

import br.com.globalti.prova.domain.Role;

import java.time.LocalDateTime;

/**
 * Essa classe serve para devolver os dados de um usuário na API.
 * Repare que não tem o campo senha: ela nunca sai em nenhuma resposta.
 */
public record UserResponse(
        Long id,
        String nome,
        String email,
        Role role,
        LocalDateTime criadoEm
) {
}
