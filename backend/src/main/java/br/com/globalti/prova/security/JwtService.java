package br.com.globalti.prova.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * Essa classe serve para criar e ler os tokens JWT.
 * O segredo e o tempo de expiração vêm da configuração (variáveis de ambiente).
 */
@Service
public class JwtService {

    private final SecretKey chave;
    private final Duration expiracao;

    public JwtService(@Value("${app.jwt.secret}") String segredo,
                      @Value("${app.jwt.expiration-minutes}") long expiracaoEmMinutos) {
        // Se o segredo tiver menos de 32 caracteres, a biblioteca recusa e a API nem sobe.
        // Prefiro quebrar na inicialização do que rodar com um segredo fraco.
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracao = Duration.ofMinutes(expiracaoEmMinutos);
    }

    // O "subject" do token é o id do usuário. Uso o id (e não o email) porque o email pode ser editado.
    public String gerarToken(Long userId) {
        Date agora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(agora)
                .expiration(new Date(agora.getTime() + expiracao.toMillis()))
                .signWith(chave)
                .compact();
    }

    public long getExpiracaoEmSegundos() {
        return expiracao.toSeconds();
    }

    // Confere a assinatura e a validade do token e devolve o id do usuário.
    // Se o token for falso, estiver adulterado ou vencido, a biblioteca lança JwtException.
    public Long extrairUserId(String token) {
        String subject = Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException ex) {
            throw new JwtException("Token com subject inválido", ex);
        }
    }
}
