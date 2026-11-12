package br.com.globalti.prova.repository;

import br.com.globalti.prova.domain.Role;
import br.com.globalti.prova.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Essa interface serve para acessar a tabela de usuários no banco.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Usado para nunca deixar o sistema sem nenhum administrador.
    long countByRole(Role role);

    // Usado na edição: o email pode já ser do próprio usuário, então ignoro o id dele.
    boolean existsByEmailAndIdNot(String email, Long id);
}
