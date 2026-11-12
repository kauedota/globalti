package br.com.globalti.prova.exception;

/**
 * Essa exceção serve para quando um email errou a senha vezes demais e está bloqueado
 * por um tempo. Vira 429.
 */
public class TooManyAttemptsException extends RuntimeException {

    public TooManyAttemptsException(long minutosRestantes) {
        super("Muitas tentativas de login. Tente novamente em " + minutosRestantes
                + (minutosRestantes == 1 ? " minuto." : " minutos."));
    }
}
