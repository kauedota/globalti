package br.com.globalti.prova.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Essa classe serve para padronizar o JSON de erro que a API devolve.
 * A lista "campos" só aparece quando o erro é de validação.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        List<CampoInvalido> campos
) {

    public record CampoInvalido(String campo, String mensagem) {
    }

    public static ApiError de(int status, String erro, String mensagem, String caminho) {
        return new ApiError(LocalDateTime.now(), status, erro, mensagem, caminho, null);
    }

    public static ApiError deValidacao(String caminho, List<CampoInvalido> campos) {
        return new ApiError(LocalDateTime.now(), 400, "Bad Request",
                "Existem campos inválidos na requisição", caminho, campos);
    }
}
