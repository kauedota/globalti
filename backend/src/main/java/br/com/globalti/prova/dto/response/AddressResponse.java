package br.com.globalti.prova.dto.response;

/**
 * Essa classe serve para devolver os dados de um endereço na API.
 */
public record AddressResponse(
        Long id,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        String cep,
        Long userId
) {
}
