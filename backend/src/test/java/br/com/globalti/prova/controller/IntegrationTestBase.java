package br.com.globalti.prova.controller;

import br.com.globalti.prova.client.ViaCepClient;
import br.com.globalti.prova.client.ViaCepResponse;
import br.com.globalti.prova.domain.Role;
import br.com.globalti.prova.domain.User;
import br.com.globalti.prova.repository.AddressRepository;
import br.com.globalti.prova.repository.UserRepository;
import br.com.globalti.prova.support.PostgresTestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Essa classe serve de base para os testes de integração: sobe a aplicação inteira com um PostgreSQL
 * de verdade (Testcontainers) e deixa o ViaCEP falso, para nunca chamar a internet.
 * Antes de cada teste o banco é limpo e o ADMIN é recriado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfig.class)
@ActiveProfiles("test")
abstract class IntegrationTestBase {

    protected static final String SENHA = "senha1234";
    protected static final String EMAIL_ADMIN = "admin@teste.com";

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected AddressRepository addressRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    protected ViaCepClient viaCepClient;

    @BeforeEach
    void limparBanco() {
        addressRepository.deleteAll();
        userRepository.deleteAll();
        criarUsuario("Administrador", EMAIL_ADMIN, Role.ADMIN);

        // CEP de verdade da Av. Paulista e um CEP que o ViaCEP diz que não existe.
        given(viaCepClient.buscar("01310100")).willReturn(new ViaCepResponse(
                "01310-100", "Avenida Paulista", "de 612 a 1510 - lado par", "Bela Vista", "São Paulo", "SP", null));
        given(viaCepClient.buscar("99999999")).willReturn(new ViaCepResponse(
                null, null, null, null, null, null, true));
    }

    protected User criarUsuario(String nome, String email, Role role) {
        User user = new User();
        user.setNome(nome);
        user.setEmail(email);
        user.setSenha(passwordEncoder.encode(SENHA));
        user.setRole(role);
        return userRepository.save(user);
    }

    // Faz o login de verdade pela API e devolve o cabeçalho "Bearer ...".
    protected String bearer(String email) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "senha", SENHA))))
                .andExpect(status().isOk())
                .andReturn();
        String token = JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
        return "Bearer " + token;
    }

    protected String json(Object objeto) throws Exception {
        return objectMapper.writeValueAsString(objeto);
    }

    protected MockHttpServletRequestBuilder comJson(MockHttpServletRequestBuilder requisicao, Object corpo)
            throws Exception {
        return requisicao.contentType(MediaType.APPLICATION_JSON).content(json(corpo));
    }
}
