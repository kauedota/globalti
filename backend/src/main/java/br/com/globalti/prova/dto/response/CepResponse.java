package br.com.globalti.prova.dto.response;

/**
 * Essa classe serve para devolver ao front os dados de um CEP consultado.
 * Assim o front não precisa conhecer o formato do ViaCEP.
 */
public record CepResponse(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String cidade,
        String estado
) {
}
