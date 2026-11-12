package br.com.globalti.prova.mapper;

import br.com.globalti.prova.domain.User;
import br.com.globalti.prova.dto.response.UserResponse;
import org.springframework.stereotype.Component;

/**
 * Essa classe serve para converter User (entidade) em UserResponse (o que vai para o cliente).
 * Escrevi na mão para ficar claro quais campos saem na API.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getRole(),
                user.getCriadoEm()
        );
    }
}
