package br.com.globalti.prova.client;

import br.com.globalti.prova.exception.CepServiceUnavailableException;
import br.com.globalti.prova.exception.InvalidCepException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Essa classe serve para testar o ViaCepClient com um servidor falso (MockRestServiceServer).
 * Nenhum teste aqui chama a internet de verdade.
 */
class ViaCepClientTest {

    private MockRestServiceServer servidor;
    private ViaCepClient client;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://viacep.teste/ws");
        servidor = MockRestServiceServer.bindTo(builder).build();
        client = new ViaCepClient(builder.build());
    }

    @Test
    void leOJsonDoViaCep() {
        servidor.expect(requestTo("http://viacep.teste/ws/01310100/json/"))
                .andRespond(withSuccess("""
                        {"cep":"01310-100","logradouro":"Avenida Paulista","complemento":"",
                         "bairro":"Bela Vista","localidade":"São Paulo","uf":"SP","ibge":"3550308"}
                        """, MediaType.APPLICATION_JSON));

        ViaCepResponse resposta = client.buscar("01310100");

        assertThat(resposta.logradouro()).isEqualTo("Avenida Paulista");
        assertThat(resposta.localidade()).isEqualTo("São Paulo");
        assertThat(resposta.naoEncontrado()).isFalse();
    }

    @Test
    void reconheceORetornoDeCepInexistente() {
        servidor.expect(requestTo("http://viacep.teste/ws/99999999/json/"))
                .andRespond(withSuccess("{\"erro\": true}", MediaType.APPLICATION_JSON));

        assertThat(client.buscar("99999999").naoEncontrado()).isTrue();
    }

    @Test
    void erro400DoViaCepViraCepInvalido() {
        servidor.expect(requestTo("http://viacep.teste/ws/00000000/json/"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> client.buscar("00000000")).isInstanceOf(InvalidCepException.class);
    }

    @Test
    void erro500DoViaCepViraServicoIndisponivel() {
        servidor.expect(requestTo("http://viacep.teste/ws/01310100/json/"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.buscar("01310100")).isInstanceOf(CepServiceUnavailableException.class);
    }

    @Test
    void respostaIlegivelViraServicoIndisponivel() {
        servidor.expect(requestTo("http://viacep.teste/ws/01310100/json/"))
                .andRespond(withSuccess("<html>não é json</html>", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.buscar("01310100")).isInstanceOf(CepServiceUnavailableException.class);
    }
}
