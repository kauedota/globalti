package br.com.globalti.prova.security;

import br.com.globalti.prova.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Essa classe serve para devolver 401 e 403 no mesmo formato JSON do resto da API.
 * O GlobalExceptionHandler não enxerga erros que acontecem dentro dos filtros do Spring Security,
 * por isso preciso disso aqui.
 */
@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public SecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Chamado quando alguém sem token (ou com token inválido) tenta acessar uma rota protegida.
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        escrever(response, request, HttpStatus.UNAUTHORIZED,
                "Você precisa estar autenticado para acessar este recurso");
    }

    // Chamado quando o usuário está logado, mas não tem permissão.
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        escrever(response, request, HttpStatus.FORBIDDEN,
                "Você não tem permissão para acessar este recurso");
    }

    private void escrever(HttpServletResponse response, HttpServletRequest request,
                          HttpStatus status, String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiError erro = ApiError.de(status.value(), status.getReasonPhrase(), mensagem, request.getRequestURI());
        objectMapper.writeValue(response.getWriter(), erro);
    }
}
