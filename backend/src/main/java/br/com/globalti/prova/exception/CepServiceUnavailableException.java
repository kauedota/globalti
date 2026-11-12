package br.com.globalti.prova.exception;

/**
 * Essa exceção serve para quando o ViaCEP está fora do ar ou demorou demais. Vira 503.
 */
public class CepServiceUnavailableException extends RuntimeException {

    public CepServiceUnavailableException(Throwable causa) {
        super("O serviço de consulta de CEP está indisponível no momento. Tente novamente em instantes.", causa);
    }
}
