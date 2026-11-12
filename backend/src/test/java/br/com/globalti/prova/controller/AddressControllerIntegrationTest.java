package br.com.globalti.prova.controller;

import br.com.globalti.prova.client.ViaCepResponse;
import br.com.globalti.prova.domain.Role;
import br.com.globalti.prova.domain.User;
import br.com.globalti.prova.exception.CepServiceUnavailableException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Essa classe serve para testar o CRUD de endereços, o preenchimento pelo ViaCEP
 * e a regra de que cada usuário só mexe nos próprios endereços.
 */
class AddressControllerIntegrationTest extends IntegrationTestBase {

    @Test
    void criaEnderecoPreenchendoOsCamposPeloViaCep() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);

        mockMvc.perform(comJson(post("/api/users/" + ana.getId() + "/addresses")
                                .header("Authorization", bearer("ana@teste.com")),
                        Map.of("cep", "01310100", "numero", "1000", "complemento", "Sala 12")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.logradouro").value("Avenida Paulista"))
                .andExpect(jsonPath("$.bairro").value("Bela Vista"))
                .andExpect(jsonPath("$.cidade").value("São Paulo"))
                .andExpect(jsonPath("$.estado").value("SP"))
                .andExpect(jsonPath("$.complemento").value("Sala 12"))
                .andExpect(jsonPath("$.userId").value(ana.getId()));
    }

    @Test
    void cepComHifenEhGuardadoSoComOitoDigitos() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);

        mockMvc.perform(comJson(post("/api/users/" + ana.getId() + "/addresses")
                                .header("Authorization", bearer("ana@teste.com")),
                        Map.of("cep", "01310-100", "numero", "1000")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cep").value("01310100"));

        assertThat(addressRepository.findAll()).extracting("cep").containsExactly("01310100");
    }

    @Test
    void valoresEnviadosPeloClienteTemPrioridadeSobreOViaCep() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);

        mockMvc.perform(comJson(post("/api/users/" + ana.getId() + "/addresses")
                                .header("Authorization", bearer("ana@teste.com")),
                        Map.of("cep", "01310100", "numero", "1", "logradouro", "Rua do Cliente", "estado", "rj")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.logradouro").value("Rua do Cliente"))
                .andExpect(jsonPath("$.estado").value("RJ"))
                .andExpect(jsonPath("$.bairro").value("Bela Vista"));
    }

    @Test
    void cepMalFormadoDa400ComOCampoNaResposta() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);

        mockMvc.perform(comJson(post("/api/users/" + ana.getId() + "/addresses")
                                .header("Authorization", bearer("ana@teste.com")),
                        Map.of("cep", "123", "numero", "10")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("cep"));
    }

    @Test
    void cepQueNaoExisteNoViaCepDa400() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);

        mockMvc.perform(comJson(post("/api/users/" + ana.getId() + "/addresses")
                                .header("Authorization", bearer("ana@teste.com")),
                        Map.of("cep", "99999999", "numero", "10")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("CEP não encontrado: 99999999"));
        assertThat(addressRepository.count()).isZero();
    }

    @Test
    void viaCepForaDoArDa503EnaoDa500() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);
        given(viaCepClient.buscar(anyString())).willThrow(new CepServiceUnavailableException(new RuntimeException("timeout")));

        mockMvc.perform(comJson(post("/api/users/" + ana.getId() + "/addresses")
                                .header("Authorization", bearer("ana@teste.com")),
                        Map.of("cep", "01310100", "numero", "10")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void usuarioComumNaoCriaEnderecoParaOutroUsuario() throws Exception {
        criarUsuario("Ana", "ana@teste.com", Role.USER);
        User bia = criarUsuario("Bia", "bia@teste.com", Role.USER);

        mockMvc.perform(comJson(post("/api/users/" + bia.getId() + "/addresses")
                                .header("Authorization", bearer("ana@teste.com")),
                        Map.of("cep", "01310100", "numero", "10")))
                .andExpect(status().isForbidden());
        assertThat(addressRepository.count()).isZero();
    }

    @Test
    void adminCriaEnderecoParaQualquerUsuario() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);

        mockMvc.perform(comJson(post("/api/users/" + ana.getId() + "/addresses")
                                .header("Authorization", bearer(EMAIL_ADMIN)),
                        Map.of("cep", "01310100", "numero", "10")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(ana.getId()));
    }

    @Test
    void criarParaUsuarioInexistenteDa404() throws Exception {
        mockMvc.perform(comJson(post("/api/users/999999/addresses").header("Authorization", bearer(EMAIL_ADMIN)),
                        Map.of("cep", "01310100", "numero", "10")))
                .andExpect(status().isNotFound());
    }

    @Test
    void listaOsEnderecosDoUsuarioPaginadosEOrdenados() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);
        String token = bearer("ana@teste.com");
        for (String cidade : new String[]{"Santos", "Campinas", "Atibaia"}) {
            mockMvc.perform(comJson(post("/api/users/" + ana.getId() + "/addresses").header("Authorization", token),
                    Map.of("cep", "01310100", "numero", "1", "cidade", cidade))).andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/users/" + ana.getId() + "/addresses?size=2&sort=cidade,asc")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].cidade").value(contains("Atibaia", "Campinas")))
                .andExpect(jsonPath("$.page.totalElements").value(3));
    }

    @Test
    void usuarioComumNaoListaEnderecosDeOutroUsuario() throws Exception {
        criarUsuario("Ana", "ana@teste.com", Role.USER);
        User bia = criarUsuario("Bia", "bia@teste.com", Role.USER);

        mockMvc.perform(get("/api/users/" + bia.getId() + "/addresses").header("Authorization", bearer("ana@teste.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void ordenarEnderecosPorCampoInvalidoDa400() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);

        mockMvc.perform(get("/api/users/" + ana.getId() + "/addresses?sort=user").header("Authorization", bearer("ana@teste.com")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usuarioComumNaoVeNemEditaNemExcluiEnderecoDeOutroUsuario() throws Exception {
        criarUsuario("Ana", "ana@teste.com", Role.USER);
        User bia = criarUsuario("Bia", "bia@teste.com", Role.USER);
        String tokenBia = bearer("bia@teste.com");
        String idEndereco = criarEndereco(bia.getId(), tokenBia);
        String tokenAna = bearer("ana@teste.com");

        mockMvc.perform(get("/api/addresses/" + idEndereco).header("Authorization", tokenAna))
                .andExpect(status().isForbidden());
        mockMvc.perform(comJson(put("/api/addresses/" + idEndereco).header("Authorization", tokenAna),
                        Map.of("cep", "01310100", "numero", "999")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/addresses/" + idEndereco).header("Authorization", tokenAna))
                .andExpect(status().isForbidden());

        assertThat(addressRepository.count()).isEqualTo(1);
    }

    @Test
    void donoVeEditaEExcluiOProprioEndereco() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);
        String token = bearer("ana@teste.com");
        String id = criarEndereco(ana.getId(), token);

        mockMvc.perform(get("/api/addresses/" + id).header("Authorization", token))
                .andExpect(status().isOk());
        mockMvc.perform(comJson(put("/api/addresses/" + id).header("Authorization", token),
                        Map.of("cep", "01310100", "numero", "2000", "complemento", "Andar 3")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("2000"))
                .andExpect(jsonPath("$.complemento").value("Andar 3"));
        mockMvc.perform(delete("/api/addresses/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/addresses/" + id).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminEditaEExcluiEnderecoDeQualquerUsuario() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);
        String id = criarEndereco(ana.getId(), bearer("ana@teste.com"));
        String admin = bearer(EMAIL_ADMIN);

        mockMvc.perform(comJson(put("/api/addresses/" + id).header("Authorization", admin),
                        Map.of("cep", "01310100", "numero", "77")))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/addresses/" + id).header("Authorization", admin))
                .andExpect(status().isNoContent());
    }

    @Test
    void consultaDeCepDevolveOsDadosEUsaOMock() throws Exception {
        mockMvc.perform(get("/api/cep/01310-100").header("Authorization", bearer(EMAIL_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01310100"))
                .andExpect(jsonPath("$.logradouro").value("Avenida Paulista"))
                .andExpect(jsonPath("$.cidade").value("São Paulo"));
    }

    @Test
    void consultaDeCepInvalidoDa400() throws Exception {
        mockMvc.perform(get("/api/cep/abc").header("Authorization", bearer(EMAIL_ADMIN)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultaDeCepExigeLogin() throws Exception {
        mockMvc.perform(get("/api/cep/01310100")).andExpect(status().isUnauthorized());
    }

    @Test
    void cepSemLogradouroNemBairroExigeQueOClienteInforme() throws Exception {
        User ana = criarUsuario("Ana", "ana@teste.com", Role.USER);
        given(viaCepClient.buscar("12345678")).willReturn(
                new ViaCepResponse("12345-678", "", "", "", "Cidadezinha", "MG", null));
        String token = bearer("ana@teste.com");

        mockMvc.perform(comJson(post("/api/users/" + ana.getId() + "/addresses").header("Authorization", token),
                        Map.of("cep", "12345678", "numero", "1")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(comJson(post("/api/users/" + ana.getId() + "/addresses").header("Authorization", token),
                        Map.of("cep", "12345678", "numero", "1", "logradouro", "Rua Única", "bairro", "Centro")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cidade").value("Cidadezinha"));
    }

    // Cria um endereço pela API e devolve o id dele.
    private String criarEndereco(Long userId, String token) throws Exception {
        String resposta = mockMvc.perform(comJson(post("/api/users/" + userId + "/addresses")
                                .header("Authorization", token),
                        Map.of("cep", "01310100", "numero", "1000")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return String.valueOf((Integer) com.jayway.jsonpath.JsonPath.read(resposta, "$.id"));
    }
}
