package br.com.globalti.prova.client;

import br.com.globalti.prova.exception.CepServiceUnavailableException;
import br.com.globalti.prova.exception.InvalidCepException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Essa classe serve para chamar o serviço externo ViaCEP (https://viacep.com.br/ws/{cep}/json/).
 * Ela só fala com a internet; as regras ficam no CepService.
 */
@Component
public class ViaCepClient {

    private final RestClient restClient;

    public ViaCepClient(RestClient viaCepRestClient) {
        this.restClient = viaCepRestClient;
    }

    // Recebe o CEP já normalizado (8 dígitos).
    public ViaCepResponse buscar(String cep) {
        try {
            ViaCepResponse resposta = restClient.get()
                    .uri("/{cep}/json/", cep)
                    .retrieve()
                    .body(ViaCepResponse.class);

            if (resposta == null) {
                throw new CepServiceUnavailableException(new IllegalStateException("Resposta vazia do ViaCEP"));
            }
            return resposta;
        } catch (RestClientResponseException ex) {
            // O ViaCEP responde 400 quando não gosta do formato do CEP; qualquer 5xx é problema do lado dele.
            if (ex.getStatusCode().is4xxClientError()) {
                throw new InvalidCepException("CEP inválido");
            }
            throw new CepServiceUnavailableException(ex);
        } catch (RestClientException ex) {
            // Timeout, conexão recusada, JSON ilegível... tudo isso eu trato como "serviço indisponível".
            throw new CepServiceUnavailableException(ex);
        }
    }
}
