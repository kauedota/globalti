package br.com.globalti.prova.repository;

import br.com.globalti.prova.domain.Address;
import br.com.globalti.prova.domain.Role;
import br.com.globalti.prova.domain.User;
import br.com.globalti.prova.support.PostgresTestConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Essa classe serve para testar o AddressRepository e a relação 1 usuário → N endereços.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestConfig.class)
@ActiveProfiles("test")
class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void listaSomenteOsEnderecosDoUsuarioInformado() {
        User ana = salvarUsuario("ana@teste.com");
        User bia = salvarUsuario("bia@teste.com");
        addressRepository.save(endereco(ana, "Rua A", "Campinas"));
        addressRepository.save(endereco(ana, "Rua B", "Atibaia"));
        addressRepository.saveAndFlush(endereco(bia, "Rua C", "Santos"));

        Page<Address> pagina = addressRepository.findByUserId(ana.getId(), PageRequest.of(0, 10, Sort.by("cidade")));

        assertThat(pagina.getContent()).extracting(Address::getCidade).containsExactly("Atibaia", "Campinas");
        assertThat(addressRepository.countByUserId(bia.getId())).isEqualTo(1);
    }

    @Test
    void paginaOsEnderecos() {
        User ana = salvarUsuario("ana@teste.com");
        for (int i = 1; i <= 5; i++) {
            addressRepository.save(endereco(ana, "Rua " + i, "Cidade " + i));
        }
        addressRepository.flush();

        Page<Address> segundaPagina = addressRepository.findByUserId(ana.getId(), PageRequest.of(1, 2, Sort.by("id")));

        assertThat(segundaPagina.getTotalElements()).isEqualTo(5);
        assertThat(segundaPagina.getContent()).hasSize(2);
    }

    @Test
    void apagarOUsuarioApagaOsEnderecosDele() {
        User ana = salvarUsuario("ana@teste.com");
        addressRepository.saveAndFlush(endereco(ana, "Rua A", "Campinas"));
        entityManager.clear();

        userRepository.deleteById(ana.getId());
        userRepository.flush();

        assertThat(addressRepository.count()).isZero();
    }

    private User salvarUsuario(String email) {
        User user = new User();
        user.setNome("Fulano");
        user.setEmail(email);
        user.setSenha("$2a$10$hashqualquerdeteste");
        user.setRole(Role.USER);
        return userRepository.saveAndFlush(user);
    }

    private Address endereco(User dono, String logradouro, String cidade) {
        Address address = new Address();
        address.setUser(dono);
        address.setLogradouro(logradouro);
        address.setNumero("10");
        address.setBairro("Centro");
        address.setCidade(cidade);
        address.setEstado("SP");
        address.setCep("13000000");
        return address;
    }
}
