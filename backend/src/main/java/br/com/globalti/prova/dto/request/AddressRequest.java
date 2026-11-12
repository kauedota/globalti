package br.com.globalti.prova.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Essa classe serve para receber os dados de um endereço (cadastro e edição).
 * Logradouro, bairro, cidade e estado são opcionais: se o cliente não mandar,
 * eu preencho com o que o ViaCEP devolver.
 */
public record AddressRequest(
        @NotBlank(message = "O CEP é obrigatório")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido: use 8 dígitos, com ou sem hífen")
        String cep,

        @NotBlank(message = "O número é obrigatório")
        @Size(max = 20, message = "O número pode ter no máximo 20 caracteres")
        String numero,

        @Size(max = 100, message = "O complemento pode ter no máximo 100 caracteres")
        String complemento,

        @Size(max = 200, message = "O logradouro pode ter no máximo 200 caracteres")
        String logradouro,

        @Size(max = 100, message = "O bairro pode ter no máximo 100 caracteres")
        String bairro,

        @Size(max = 100, message = "A cidade pode ter no máximo 100 caracteres")
        String cidade,

        @Pattern(regexp = "^$|[A-Za-z]{2}", message = "O estado deve ter 2 letras (ex.: SP)")
        String estado
) {
}
