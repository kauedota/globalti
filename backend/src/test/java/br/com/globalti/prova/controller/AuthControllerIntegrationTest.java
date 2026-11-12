package br.com.globalti.prova.controller;

import br.com.globalti.prova.domain.Role;
import br.com.globalti.prova.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Essa classe serve para testar registro e login pela API de ponta a ponta.
 */
class AuthControllerIntegrationTest extends IntegrationTestBase {

    @Test
    void registraUsuarioComumEDevolveToken() throws Exception {
        mockMvc.perform(comJson(post("/api/auth/register"),
                        Map.of("nome", "Ana Souza", "email", "ana@teste.com", "senha", SENHA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.email").value("ana@teste.com"))
                .andExpect(jsonPath("$.usuario.role").value("USER"))
                .andExpect(content().string(not(containsString("senha"))));
    }

    @Test
    void senhaFicaCriptografadaNoBanco() throws Exception {
        mockMvc.perform(comJson(post("/api/auth/register"),
                Map.of("nome", "Ana", "email", "ana@teste.com", "senha", SENHA))).andExpect(status().isCreated());

        User salva = userRepository.findByEmail("ana@teste.com").orElseThrow();
        assertThat(salva.getSenha()).isNotEqualTo(SENHA).startsWith("$2");
    }

    @Test
    void registroPublicoIgnoraTentativaDeVirarAdmin() throws Exception {
        mockMvc.perform(comJson(post("/api/auth/register"),
                        Map.of("nome", "Esperto", "email", "esperto@teste.com", "senha", SENHA, "role", "ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.role").value("USER"));

        assertThat(userRepository.findByEmail("esperto@teste.com").orElseThrow().getRole()).isEqualTo(Role.USER);
    }

    @Test
    void registroComEmailRepetidoDa409() throws Exception {
        criarUsuario("Ana", "ana@teste.com", Role.USER);

        mockMvc.perform(comJson(post("/api/auth/register"),
                        Map.of("nome", "Outra Ana", "email", "ANA@teste.com", "senha", SENHA)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.caminho").value("/api/auth/register"));
    }

    @Test
    void registroInvalidoDa400ListandoOsCampos() throws Exception {
        mockMvc.perform(comJson(post("/api/auth/register"),
                        Map.of("nome", "", "email", "nao-e-email", "senha", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos", hasSize(3)))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void corpoMalFormadoDa400() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType("application/json").content("{ isso nao e json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginComCredenciaisCertasDevolveToken() throws Exception {
        criarUsuario("Ana", "ana@teste.com", Role.USER);

        mockMvc.perform(comJson(post("/api/auth/login"), Map.of("email", "ana@teste.com", "senha", SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.nome").value("Ana"));
    }

    @Test
    void loginComSenhaErradaDa401() throws Exception {
        criarUsuario("Ana", "ana@teste.com", Role.USER);

        mockMvc.perform(comJson(post("/api/auth/login"), Map.of("email", "ana@teste.com", "senha", "errada123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Email ou senha inválidos"));
    }

    @Test
    void loginComEmailInexistenteDa401() throws Exception {
        mockMvc.perform(comJson(post("/api/auth/login"), Map.of("email", "ninguem@teste.com", "senha", SENHA)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotaProtegidaSemTokenDa401EmJson() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void rotaProtegidaComTokenInvalidoDa401() throws Exception {
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer isso.nao.eh.um.token"))
                .andExpect(status().isUnauthorized());
    }
}
