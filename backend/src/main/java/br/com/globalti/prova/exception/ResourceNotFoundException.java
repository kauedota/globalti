package br.com.globalti.prova.exception;

/**
 * Essa exceção serve para avisar que um recurso (usuário, endereço...) não existe. Vira 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensagem) {
        super(mensagem);
    }
}
