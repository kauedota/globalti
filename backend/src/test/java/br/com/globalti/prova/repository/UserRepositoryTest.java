package br.com.globalti.prova.repository;

import br.com.globalti.prova.domain.Role;
import br.com.globalti.prova.domain.User;
import br.com.globalti.prova.support.PostgresTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Essa classe serve para testar o UserRepository contra um PostgreSQL real (Testcontainers),
 * já com as tabelas criadas pelo Flyway.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestConfig.class)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void salvaEPreencheADataDeCriacaoSozinho() {
        User salvo = userRepository.saveAndFlush(usuario("Ana", "ana@teste.com"));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getCriadoEm()).isNotNull();
    }

    @Test
    void buscaPorEmail() {
        userRepository.saveAndFlush(usuario("Ana", "ana@teste.com"));

        assertThat(userRepository.findByEmail("ana@teste.com")).isPresent();
        assertThat(userRepository.findByEmail("outro@teste.com")).isEmpty();
    }

    @Test
    void naoPermiteDoisUsuariosComOMesmoEmail() {
        userRepository.saveAndFlush(usuario("Ana", "ana@teste.com"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(usuario("Outra Ana", "ana@teste.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void existsByEmailAndIdNotIgnoraOProprioUsuario() {
        User ana = userRepository.saveAndFlush(usuario("Ana", "ana@teste.com"));
        User bia = userRepository.saveAndFlush(usuario("Bia", "bia@teste.com"));

        assertThat(userRepository.existsByEmailAndIdNot("ana@teste.com", ana.getId())).isFalse();
        assertThat(userRepository.existsByEmailAndIdNot("ana@teste.com", bia.getId())).isTrue();
    }

    @Test
    void paginaEOrdenaPorNome() {
        userRepository.save(usuario("Carla", "c@teste.com"));
        userRepository.save(usuario("Ana", "a@teste.com"));
        userRepository.saveAndFlush(usuario("Bia", "b@teste.com"));

        Page<User> pagina = userRepository.findAll(PageRequest.of(0, 2, Sort.by("nome")));

        assertThat(pagina.getTotalElements()).isEqualTo(3);
        assertThat(pagina.getTotalPages()).isEqualTo(2);
        assertThat(pagina.getContent()).extracting(User::getNome).containsExactly("Ana", "Bia");
    }

    @Test
    void ordenaPorEmailDecrescente() {
        userRepository.save(usuario("Ana", "a@teste.com"));
        userRepository.saveAndFlush(usuario("Bia", "b@teste.com"));

        Page<User> pagina = userRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "email")));

        assertThat(pagina.getContent()).extracting(User::getEmail).containsExactly("b@teste.com", "a@teste.com");
    }

    @Test
    void ordenaPorDataDeCriacao() {
        User primeiro = userRepository.saveAndFlush(usuario("Zeca", "z@teste.com"));
        User segundo = userRepository.saveAndFlush(usuario("Ana", "a@teste.com"));

        Page<User> pagina = userRepository.findAll(PageRequest.of(0, 10, Sort.by("criadoEm")));

        assertThat(pagina.getContent()).extracting(User::getId)
                .containsExactly(primeiro.getId(), segundo.getId());
    }

    private User usuario(String nome, String email) {
        User user = new User();
        user.setNome(nome);
        user.setEmail(email);
        user.setSenha("$2a$10$hashqualquerdeteste");
        user.setRole(Role.USER);
        return user;
    }
}
