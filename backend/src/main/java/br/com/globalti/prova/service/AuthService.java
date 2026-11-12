package br.com.globalti.prova.service;

import br.com.globalti.prova.dto.request.LoginRequest;
import br.com.globalti.prova.dto.request.RegisterRequest;
import br.com.globalti.prova.dto.response.AuthResponse;
import br.com.globalti.prova.dto.response.UserResponse;
import br.com.globalti.prova.security.JwtService;
import br.com.globalti.prova.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Essa classe serve para o registro e o login: confere as credenciais e entrega o token JWT.
 */
@Service
public class AuthService {

    private static final String TIPO_TOKEN = "Bearer";

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserService userService, AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // Registro público: cria um usuário comum e já devolve o token, para a pessoa entrar direto.
    public AuthResponse registrar(RegisterRequest request) {
        UserResponse usuario = userService.registrar(request);
        return montarResposta(usuario);
    }

    // O AuthenticationManager compara a senha com o hash BCrypt do banco.
    // Se estiver errada, ele lança BadCredentialsException, que o handler global transforma em 401.
    public AuthResponse login(LoginRequest request) {
        Authentication autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.senha()));

        UserPrincipal principal = (UserPrincipal) autenticacao.getPrincipal();
        return montarResposta(userService.buscar(principal.getId(), principal));
    }

    private AuthResponse montarResposta(UserResponse usuario) {
        String token = jwtService.gerarToken(usuario.id());
        return new AuthResponse(token, TIPO_TOKEN, jwtService.getExpiracaoEmSegundos(), usuario);
    }
}
