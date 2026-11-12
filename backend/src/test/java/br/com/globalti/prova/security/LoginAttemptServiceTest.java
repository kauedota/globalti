package br.com.globalti.prova.security;

import br.com.globalti.prova.exception.TooManyAttemptsException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Essa classe serve para testar o bloqueio de login depois de muitas senhas erradas.
 * Uso um relógio controlado para "passar o tempo" sem precisar esperar.
 */
class LoginAttemptServiceTest {

    // Relógio que eu mesmo adianto nos testes.
    private static class RelogioControlado extends Clock {
        private Instant agora = Instant.parse("2026-01-01T10:00:00Z");

        void avancar(Duration tempo) {
            agora = agora.plus(tempo);
        }

        @Override
        public Instant instant() {
            return agora;
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
    }

    private final RelogioControlado relogio = new RelogioControlado();
    private final LoginAttemptService servico = new LoginAttemptService(3, 15, relogio);

    @Test
    void naoBloqueiaAbaixoDoLimiteDeFalhas() {
        servico.registrarFalha("ana@teste.com");
        servico.registrarFalha("ana@teste.com");

        assertThatCode(() -> servico.verificarBloqueio("ana@teste.com")).doesNotThrowAnyException();
    }

    @Test
    void bloqueiaDepoisDoLimiteDeFalhas() {
        for (int i = 0; i < 3; i++) {
            servico.registrarFalha("ana@teste.com");
        }

        assertThatThrownBy(() -> servico.verificarBloqueio("ana@teste.com"))
                .isInstanceOf(TooManyAttemptsException.class)
                .hasMessageContaining("15 minutos");
    }

    @Test
    void bloqueioDeUmEmailNaoAfetaOutro() {
        for (int i = 0; i < 3; i++) {
            servico.registrarFalha("ana@teste.com");
        }

        assertThatCode(() -> servico.verificarBloqueio("bia@teste.com")).doesNotThrowAnyException();
    }

    @Test
    void desbloqueiaDepoisDoTempoDeBloqueio() {
        for (int i = 0; i < 3; i++) {
            servico.registrarFalha("ana@teste.com");
        }

        relogio.avancar(Duration.ofMinutes(16));

        assertThatCode(() -> servico.verificarBloqueio("ana@teste.com")).doesNotThrowAnyException();
    }

    @Test
    void loginComSucessoZeraAsFalhas() {
        servico.registrarFalha("ana@teste.com");
        servico.registrarFalha("ana@teste.com");
        servico.registrarSucesso("ana@teste.com");
        servico.registrarFalha("ana@teste.com");
        servico.registrarFalha("ana@teste.com");

        assertThatCode(() -> servico.verificarBloqueio("ana@teste.com")).doesNotThrowAnyException();
    }

    @Test
    void falhasAntigasNaoContam() {
        servico.registrarFalha("ana@teste.com");
        servico.registrarFalha("ana@teste.com");

        relogio.avancar(Duration.ofMinutes(20));
        servico.registrarFalha("ana@teste.com");

        assertThatCode(() -> servico.verificarBloqueio("ana@teste.com")).doesNotThrowAnyException();
    }
}
