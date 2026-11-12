package br.com.globalti.prova.exception;

/**
 * Essa exceção serve para requisições inválidas que a validação de campos não pega
 * (ex.: ordenar por um campo que não existe). Vira 400.
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String mensagem) {
        super(mensagem);
    }
}
