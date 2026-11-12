package br.com.globalti.prova.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Essa classe serve para capturar todas as exceções da API em um lugar só
 * e transformar cada uma em uma resposta JSON padronizada (ApiError).
 * Assim o front sempre recebe o mesmo formato e nunca vê stacktrace.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Erros de validação do @Valid: junto cada campo inválido com a sua mensagem.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> tratarValidacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ApiError.CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ApiError.CampoInvalido(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(ApiError.deValidacao(req.getRequestURI(), campos));
    }

    // Validação de parâmetros soltos (ex.: @PathVariable com restrição).
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> tratarRestricoes(ConstraintViolationException ex, HttpServletRequest req) {
        List<ApiError.CampoInvalido> campos = ex.getConstraintViolations().stream()
                .map(v -> new ApiError.CampoInvalido(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return ResponseEntity.badRequest().body(ApiError.deValidacao(req.getRequestURI(), campos));
    }

    @ExceptionHandler({InvalidRequestException.class, InvalidCepException.class})
    public ResponseEntity<ApiError> tratarRequisicaoInvalida(RuntimeException ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    // JSON quebrado, tipo errado, enum desconhecido, etc.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> tratarCorpoIlegivel(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "O corpo da requisição está inválido ou mal formatado", req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> tratarTipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "O parâmetro '" + ex.getName() + "' tem um valor inválido", req);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> tratarParametroFaltando(MissingServletRequestParameterException ex,
                                                            HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "O parâmetro '" + ex.getParameterName() + "' é obrigatório", req);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> tratarCredenciais(BadCredentialsException ex, HttpServletRequest req) {
        return montar(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos", req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> tratarAutenticacao(AuthenticationException ex, HttpServletRequest req) {
        return montar(HttpStatus.UNAUTHORIZED, "Você precisa estar autenticado para acessar este recurso", req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> tratarAcessoNegado(AccessDeniedException ex, HttpServletRequest req) {
        return montar(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso", req);
    }

    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiError> tratarNaoEncontrado(Exception ex, HttpServletRequest req) {
        String mensagem = ex instanceof ResourceNotFoundException ? ex.getMessage() : "Recurso não encontrado";
        return montar(HttpStatus.NOT_FOUND, mensagem, req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex,
                                                             HttpServletRequest req) {
        return montar(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não suportado para este endereço", req);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> tratarEmailDuplicado(EmailAlreadyExistsException ex, HttpServletRequest req) {
        return montar(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    // Rede de segurança: se duas requisições com o mesmo email chegarem juntas,
    // a checagem do service passa nas duas e quem barra é a constraint do banco.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> tratarIntegridade(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Violação de integridade no banco: {}", ex.getMostSpecificCause().getMessage());
        return montar(HttpStatus.CONFLICT, "Os dados informados conflitam com um registro já existente", req);
    }

    @ExceptionHandler(CepServiceUnavailableException.class)
    public ResponseEntity<ApiError> tratarCepIndisponivel(CepServiceUnavailableException ex, HttpServletRequest req) {
        log.warn("ViaCEP indisponível: {}", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
        return montar(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), req);
    }

    // Qualquer erro que eu não previ: registro o detalhe no log, mas devolvo uma mensagem genérica.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> tratarGenerico(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado em {}", req.getRequestURI(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado. Tente novamente mais tarde", req);
    }

    private ResponseEntity<ApiError> montar(HttpStatus status, String mensagem, HttpServletRequest req) {
        ApiError corpo = ApiError.de(status.value(), status.getReasonPhrase(), mensagem, req.getRequestURI());
        return ResponseEntity.status(status).body(corpo);
    }
}
