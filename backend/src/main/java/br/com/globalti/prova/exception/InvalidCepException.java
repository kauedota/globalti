package br.com.globalti.prova.exception;

/**
 * Essa exceção serve para CEP mal formado ou que não existe no ViaCEP. Vira 400.
 */
public class InvalidCepException extends RuntimeException {

    public InvalidCepException(String mensagem) {
        super(mensagem);
    }
}
