package br.com.globalti.prova.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Essa classe serve para ler o JSON que o ViaCEP devolve.
 * Os nomes dos campos são os do ViaCEP (localidade = cidade, uf = estado).
 * Quando o CEP não existe, o ViaCEP responde só {"erro": true}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponse(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        Boolean erro
) {

    public boolean naoEncontrado() {
        return Boolean.TRUE.equals(erro);
    }
}
