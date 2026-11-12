package br.com.globalti.prova.exception;

/**
 * Essa exceção serve para avisar que o email já está cadastrado. Vira 409.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Já existe um usuário cadastrado com o email " + email);
    }
}
