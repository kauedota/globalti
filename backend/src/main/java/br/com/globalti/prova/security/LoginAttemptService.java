package br.com.globalti.prova.security;

import br.com.globalti.prova.exception.TooManyAttemptsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Essa classe serve para frear ataque de força bruta no login: depois de algumas senhas erradas
 * seguidas para o mesmo email, ele fica bloqueado por um tempo.
 * Guardo tudo na memória, o que basta para uma instância só. Com várias instâncias da API,
 * o certo seria guardar isso em um lugar compartilhado (ex.: Redis).
 */
@Service
public class LoginAttemptService {

    private static class Registro {
        int falhas;
        Instant primeiraFalha;
        Instant bloqueadoAte;
    }

    private final ConcurrentHashMap<String, Registro> registros = new ConcurrentHashMap<>();
    private final int maximoDeFalhas;
    private final Duration tempoDeBloqueio;
    private final Clock relogio;

    public LoginAttemptService(@Value("${app.login.max-attempts}") int maximoDeFalhas,
                               @Value("${app.login.block-minutes}") long minutosDeBloqueio,
                               Clock relogio) {
        this.maximoDeFalhas = maximoDeFalhas;
        this.tempoDeBloqueio = Duration.ofMinutes(minutosDeBloqueio);
        this.relogio = relogio;
    }

    // Chamado ANTES de conferir a senha: se o email está bloqueado, nem tento autenticar.
    public void verificarBloqueio(String email) {
        Registro registro = registros.get(email);
        if (registro == null || registro.bloqueadoAte == null) {
            return;
        }
        Instant agora = relogio.instant();
        if (agora.isBefore(registro.bloqueadoAte)) {
            // Arredondo para cima: faltando 14 min e 10 s, aviso "15 minutos".
            long minutos = (Duration.between(agora, registro.bloqueadoAte).toSeconds() + 59) / 60;
            throw new TooManyAttemptsException(minutos);
        }
        // O bloqueio já acabou: limpo o registro e a pessoa começa do zero.
        registros.remove(email, registro);
    }

    public void registrarFalha(String email) {
        Instant agora = relogio.instant();
        registros.compute(email, (chave, atual) -> {
            Registro registro = atual;
            // Falhas antigas (fora da janela) não contam mais.
            if (registro == null || registro.bloqueadoAte != null
                    || registro.primeiraFalha.plus(tempoDeBloqueio).isBefore(agora)) {
                registro = new Registro();
                registro.primeiraFalha = agora;
            }
            registro.falhas++;
            if (registro.falhas >= maximoDeFalhas) {
                registro.bloqueadoAte = agora.plus(tempoDeBloqueio);
            }
            return registro;
        });
    }

    public void registrarSucesso(String email) {
        registros.remove(email);
    }
}
