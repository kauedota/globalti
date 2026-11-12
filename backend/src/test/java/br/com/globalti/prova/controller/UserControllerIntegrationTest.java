package br.com.globalti.prova.controller;

import br.com.globalti.prova.domain.Role;
import br.com.globalti.prova.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Essa classe serve para testar o CRUD de usuários e a regra de permissão USER x ADMIN.
 */
class UserControllerIntegrationTest extends IntegrationTestBase {

    @Test
    void meDevolveOUsuarioDoToken() throws Exception {
        criarUsuario("Ana", "ana@teste.com", Role.USER);

        mockMvc.perform(get("/api/users/me").header("Authorization", bearer("ana@teste.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@teste.com"))
                .andExpect(content().string(not(containsString("senha"))));
    }

    @Test
    void adminListaTodosOsUsuariosPaginados() throws Exception {
        criarUsuario("Carla", "carla@teste.com", Role.USER);
        criarUsuario("Bia", "bia@teste.com", Role.USER);

        mockMvc.perform(get("/api/users?page=0&size=2&sort=nome,asc").header("Authorization", bearer(EMAIL_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].nome").value(contains("Administrador", "Bia")))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    @Test
    void adminOrdenaPorEmailDecrescente() throws Exception {
        criarUsuario("Carla", "carla@teste.com", Role.USER);

        mockMvc.perform(get("/api/users?sort=email,desc").header("Authorization", bearer(EMAIL_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].email").value(contains("carla@teste.com", EMAIL_ADMIN)));
    }

    @Test
    void adminOrdenaPorDataDeCriacao() throws Exception {
        criarUsuario("Zeca", "zeca@teste.com", Role.USER);

        mockMvc.perform(get("/api/users?sort=criadoEm,desc").header("Authorization", bearer(EMAIL_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("zeca@teste.com"));
    }

    @Test
    void paginaTemTamanhoPadraoDe10ELimiteDe100() throws Exception {
        String admin = bearer(EMAIL_ADMIN);

        mockMvc.perform(get("/api/users").header("Authorization", admin))
                .andExpect(jsonPath("$.page.size").value(10));
        mockMvc.perform(get("/api/users?size=5000").header("Authorization", admin))
                .andExpect(jsonPath("$.page.size").value(100));
    }

    @Test
    void ordenarPorCampoNaoPermitidoDa400() throws Exception {
        mockMvc.perform(get("/api/users?sort=senha").header("Authorization", bearer(EMAIL_ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("senha")));
    }

    @Test
    void ordenarPorCampoInexistenteDa400EnaoDa500() throws Exception {
        mockMvc.perform(get("/api/users?sort=naoExiste,asc").header("Authorization", bearer(EMAIL_ADMIN)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usuarioComumNaoListaTodosOsUsuarios() throws Exception {
        criarUsuario("Ana", "ana@teste.com", Role.USER);

        mockMvc.perform(get("/api/users").header("Authorization", bearer("ana@teste.com")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void usuarioComumVeOsProprioDados() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);

        mockMvc.perform(get("/api/users/" + ana.getId()).header("Authorization", bearer("ana@teste.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ana.getId()));
    }

    @Test
    void usuarioComumNaoVeDadosDeOutroUsuario() throws Exception {
        criarUsuario("Ana", "ana@teste.com", Role.USER);
        User bia = criarUsuario("Bia", "bia@teste.com", Role.USER);

        mockMvc.perform(get("/api/users/" + bia.getId()).header("Authorization", bearer("ana@teste.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminVeQualquerUsuarioEUsuarioInexistenteDa404() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);
        String admin = bearer(EMAIL_ADMIN);

        mockMvc.perform(get("/api/users/" + ana.getId()).header("Authorization", admin))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/users/999999").header("Authorization", admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCriaOutroAdmin() throws Exception {
        mockMvc.perform(comJson(post("/api/users").header("Authorization", bearer(EMAIL_ADMIN)),
                        Map.of("nome", "Chefe", "email", "chefe@teste.com", "senha", SENHA, "role", "ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void usuarioComumNaoCriaUsuarios() throws Exception {
        criarUsuario("Ana", "ana@teste.com", Role.USER);

        mockMvc.perform(comJson(post("/api/users").header("Authorization", bearer("ana@teste.com")),
                        Map.of("nome", "Chefe", "email", "chefe@teste.com", "senha", SENHA, "role", "ADMIN")))
                .andExpect(status().isForbidden());
        assertThat(userRepository.findByEmail("chefe@teste.com")).isEmpty();
    }

    @Test
    void usuarioEditaOProprioNomeESenha() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);

        mockMvc.perform(comJson(put("/api/users/" + ana.getId()).header("Authorization", bearer("ana@teste.com")),
                        Map.of("nome", "Ana Maria", "email", "ana@teste.com", "senha", "novaSenha99")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Maria"));

        // A senha nova tem que funcionar no login.
        mockMvc.perform(comJson(post("/api/auth/login"), Map.of("email", "ana@teste.com", "senha", "novaSenha99")))
                .andExpect(status().isOk());
    }

    @Test
    void usuarioComumNaoConsegueSeTornarAdminPelaEdicao() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);

        mockMvc.perform(comJson(put("/api/users/" + ana.getId()).header("Authorization", bearer("ana@teste.com")),
                        Map.of("nome", "Ana", "email", "ana@teste.com", "role", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void usuarioComumNaoEditaOutroUsuario() throws Exception {
        criarUsuario("Ana", "ana@teste.com", Role.USER);
        User bia = criarUsuario("Bia", "bia@teste.com", Role.USER);

        mockMvc.perform(comJson(put("/api/users/" + bia.getId()).header("Authorization", bearer("ana@teste.com")),
                        Map.of("nome", "Hackeada", "email", "bia@teste.com")))
                .andExpect(status().isForbidden());
        assertThat(userRepository.findById(bia.getId()).orElseThrow().getNome()).isEqualTo("Bia");
    }

    @Test
    void edicaoComEmailDeOutroUsuarioDa409() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);
        criarUsuario("Bia", "bia@teste.com", Role.USER);

        mockMvc.perform(comJson(put("/api/users/" + ana.getId()).header("Authorization", bearer("ana@teste.com")),
                        Map.of("nome", "Ana", "email", "bia@teste.com")))
                .andExpect(status().isConflict());
    }

    @Test
    void adminPromoveUsuarioParaAdmin() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);

        mockMvc.perform(comJson(put("/api/users/" + ana.getId()).header("Authorization", bearer(EMAIL_ADMIN)),
                        Map.of("nome", "Ana", "email", "ana@teste.com", "role", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void adminExcluiUsuarioEOsEnderecosDeleVaoJunto() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);
        mockMvc.perform(comJson(post("/api/users/" + ana.getId() + "/addresses")
                        .header("Authorization", bearer("ana@teste.com")),
                        Map.of("cep", "01310100", "numero", "1000")))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/users/" + ana.getId()).header("Authorization", bearer(EMAIL_ADMIN)))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(ana.getId())).isEmpty();
        assertThat(addressRepository.count()).isZero();
    }

    @Test
    void oUltimoAdministradorNaoPodeSerExcluidoNemRebaixado() throws Exception {
        User admin = userRepository.findByEmail(EMAIL_ADMIN).orElseThrow();
        String token = bearer(EMAIL_ADMIN);

        mockMvc.perform(delete("/api/users/" + admin.getId()).header("Authorization", token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(containsString("último administrador")));
        mockMvc.perform(comJson(put("/api/users/" + admin.getId()).header("Authorization", token),
                        Map.of("nome", "Administrador", "email", EMAIL_ADMIN, "role", "USER")))
                .andExpect(status().isConflict());

        assertThat(userRepository.findById(admin.getId()).orElseThrow().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void adminPodeExcluirOutroAdministradorQuandoSobraPeloMenosUm() throws Exception {
        User outro = criarUsuario("Outro Admin", "outro-admin@teste.com", Role.ADMIN);

        mockMvc.perform(delete("/api/users/" + outro.getId()).header("Authorization", bearer(EMAIL_ADMIN)))
                .andExpect(status().isNoContent());
    }

    @Test
    void usuarioComumNaoExcluiOutroUsuario() throws Exception {
        criarUsuario("Ana", "ana@teste.com", Role.USER);
        User bia = criarUsuario("Bia", "bia@teste.com", Role.USER);

        mockMvc.perform(delete("/api/users/" + bia.getId()).header("Authorization", bearer("ana@teste.com")))
                .andExpect(status().isForbidden());
        assertThat(userRepository.findById(bia.getId())).isPresent();
    }

    @Test
    void idQueNaoEhNumeroDa400() throws Exception {
        mockMvc.perform(get("/api/users/abc").header("Authorization", bearer(EMAIL_ADMIN)))
                .andExpect(status().isBadRequest());
    }
}
