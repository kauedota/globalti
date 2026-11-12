package br.com.globalti.prova.dto.response;

/**
 * Essa classe serve para devolver o token JWT depois do login ou do registro,
 * junto com os dados do usuário logado.
 */
public record AuthResponse(
        String token,
        String tipo,
        long expiraEmSegundos,
        UserResponse usuario
) {
}
