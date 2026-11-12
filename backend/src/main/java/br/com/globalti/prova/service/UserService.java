package br.com.globalti.prova.service;

import br.com.globalti.prova.domain.Role;
import br.com.globalti.prova.domain.User;
import br.com.globalti.prova.dto.request.RegisterRequest;
import br.com.globalti.prova.dto.request.UserCreateRequest;
import br.com.globalti.prova.dto.request.UserUpdateRequest;
import br.com.globalti.prova.dto.response.UserResponse;
import br.com.globalti.prova.exception.BusinessConflictException;
import br.com.globalti.prova.exception.EmailAlreadyExistsException;
import br.com.globalti.prova.exception.ResourceNotFoundException;
import br.com.globalti.prova.mapper.UserMapper;
import br.com.globalti.prova.repository.UserRepository;
import br.com.globalti.prova.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Essa classe serve para as regras de negócio dos usuários.
 * É aqui que eu decido quem pode mexer em quê: ADMIN mexe em qualquer usuário,
 * USER mexe só em si mesmo.
 */
@Service
public class UserService {

    private static final Set<String> CAMPOS_ORDENAVEIS = Set.of("nome", "email", "criadoEm");
    private static final Sort ORDENACAO_PADRAO = Sort.by("nome");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    // Esse método faz a paginação: o front manda a página e o tamanho, e eu devolvo só aquele pedaço.
    // Só o ADMIN pode ver a lista completa.
    @Transactional(readOnly = true)
    public Page<UserResponse> listar(Pageable pageable, UserPrincipal solicitante) {
        if (!solicitante.isAdmin()) {
            throw new AccessDeniedException("Somente administradores podem listar todos os usuários");
        }
        Pageable paginaValida = Paginacao.validar(pageable, CAMPOS_ORDENAVEIS, ORDENACAO_PADRAO);
        return userRepository.findAll(paginaValida).map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse buscar(Long id, UserPrincipal solicitante) {
        garantirAcesso(id, solicitante);
        return userMapper.toResponse(buscarEntidade(id));
    }

    // Cadastro público: o perfil é SEMPRE USER, ninguém vira ADMIN por aqui.
    @Transactional
    public UserResponse registrar(RegisterRequest request) {
        return salvarNovo(request.nome(), request.email(), request.senha(), Role.USER);
    }

    // Criação feita por um ADMIN, que pode escolher o perfil do novo usuário.
    @Transactional
    public UserResponse criar(UserCreateRequest request, UserPrincipal solicitante) {
        if (!solicitante.isAdmin()) {
            throw new AccessDeniedException("Somente administradores podem criar usuários");
        }
        Role role = request.role() != null ? request.role() : Role.USER;
        return salvarNovo(request.nome(), request.email(), request.senha(), role);
    }

    @Transactional
    public UserResponse atualizar(Long id, UserUpdateRequest request, UserPrincipal solicitante) {
        garantirAcesso(id, solicitante);
        User user = buscarEntidade(id);

        String email = normalizarEmail(request.email());
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new EmailAlreadyExistsException(email);
        }

        user.setNome(request.nome().trim());
        user.setEmail(email);

        // Senha em branco significa "não quero trocar a senha".
        if (request.senha() != null && !request.senha().isBlank()) {
            user.setSenha(passwordEncoder.encode(request.senha()));
        }

        // Só o ADMIN pode mudar o perfil. Se um USER mandar "role", eu ignoro.
        if (solicitante.isAdmin() && request.role() != null) {
            if (user.getRole() == Role.ADMIN && request.role() != Role.ADMIN) {
                garantirQueNaoEOUltimoAdmin(user, "rebaixar");
            }
            user.setRole(request.role());
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    // Os endereços do usuário são apagados junto (cascade).
    @Transactional
    public void excluir(Long id, UserPrincipal solicitante) {
        garantirAcesso(id, solicitante);
        User user = buscarEntidade(id);
        garantirQueNaoEOUltimoAdmin(user, "excluir");
        userRepository.delete(user);
    }

    // Se o sistema ficasse sem administrador, ninguém mais conseguiria gerenciar os usuários.
    // Por isso não deixo excluir nem rebaixar o último ADMIN.
    private void garantirQueNaoEOUltimoAdmin(User user, String acao) {
        if (user.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new BusinessConflictException("Não é possível " + acao + " o último administrador do sistema");
        }
    }

    private UserResponse salvarNovo(String nome, String email, String senhaPura, Role role) {
        String emailNormalizado = normalizarEmail(email);
        if (userRepository.existsByEmail(emailNormalizado)) {
            throw new EmailAlreadyExistsException(emailNormalizado);
        }

        User user = new User();
        user.setNome(nome.trim());
        user.setEmail(emailNormalizado);
        // Criptografo a senha ANTES de salvar. Nunca guardo senha pura no banco.
        user.setSenha(passwordEncoder.encode(senhaPura));
        user.setRole(role);

        return userMapper.toResponse(userRepository.save(user));
    }

    // Regra central: ADMIN passa sempre; USER só passa se o id for o dele mesmo.
    private void garantirAcesso(Long idDoUsuarioAlvo, UserPrincipal solicitante) {
        if (!solicitante.isAdmin() && !solicitante.getId().equals(idDoUsuarioAlvo)) {
            throw new AccessDeniedException("Você só pode acessar os seus próprios dados");
        }
    }

    private User buscarEntidade(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    // Email sempre minúsculo e sem espaços, para "Ana@x.com" e "ana@x.com" contarem como o mesmo.
    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
