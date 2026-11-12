package br.com.globalti.prova.exception;

/**
 * Essa exceção serve para quando a ação entra em conflito com uma regra do sistema
 * (ex.: apagar o último administrador, ou usar um email que já existe). Vira 409.
 */
public class BusinessConflictException extends RuntimeException {

    public BusinessConflictException(String mensagem) {
        super(mensagem);
    }
}
