package br.com.globalti.prova.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Essa classe serve para testar a criação e a leitura do token JWT.
 */
class JwtServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres!";

    private final JwtService jwtService = new JwtService(SEGREDO, 5);

    @Test
    void tokenGeradoDevolveOMesmoIdDoUsuario() {
        String token = jwtService.gerarToken(42L);

        assertThat(jwtService.extrairUserId(token)).isEqualTo(42L);
    }

    @Test
    void tokenAdulteradoEhRejeitado() {
        String token = jwtService.gerarToken(42L);
        String adulterado = token.substring(0, token.length() - 3) + "abc";

        assertThatThrownBy(() -> jwtService.extrairUserId(adulterado)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenAssinadoComOutroSegredoEhRejeitado() {
        String deOutraApi = new JwtService("outro-segredo-bem-diferente-e-comprido-123", 5).gerarToken(1L);

        assertThatThrownBy(() -> jwtService.extrairUserId(deOutraApi)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenVencidoEhRejeitado() {
        String vencido = new JwtService(SEGREDO, -1).gerarToken(1L);

        assertThatThrownBy(() -> jwtService.extrairUserId(vencido)).isInstanceOf(JwtException.class);
    }

    @Test
    void segredoCurtoImpedeACriacaoDoServico() {
        assertThatThrownBy(() -> new JwtService("curto", 5)).isInstanceOf(WeakKeyException.class);
    }

    @Test
    void informaAExpiracaoEmSegundos() {
        assertThat(jwtService.getExpiracaoEmSegundos()).isEqualTo(300);
    }
}
