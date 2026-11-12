package br.com.globalti.prova.service;

import br.com.globalti.prova.domain.Role;
import br.com.globalti.prova.domain.User;
import br.com.globalti.prova.dto.request.RegisterRequest;
import br.com.globalti.prova.dto.request.UserCreateRequest;
import br.com.globalti.prova.dto.request.UserUpdateRequest;
import br.com.globalti.prova.dto.response.UserResponse;
import br.com.globalti.prova.exception.EmailAlreadyExistsException;
import br.com.globalti.prova.exception.InvalidRequestException;
import br.com.globalti.prova.exception.ResourceNotFoundException;
import br.com.globalti.prova.mapper.UserMapper;
import br.com.globalti.prova.repository.UserRepository;
import br.com.globalti.prova.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Essa classe serve para testar as regras do UserService sem banco de dados (tudo com Mockito).
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    private final UserPrincipal admin = new UserPrincipal(1L, "admin@teste.com", "hash", Role.ADMIN);
    private final UserPrincipal comum = new UserPrincipal(2L, "ana@teste.com", "hash", Role.USER);

    @BeforeEach
    void preparar() {
        userService = new UserService(userRepository, passwordEncoder, new UserMapper());
    }

    @Test
    void registrarSempreCriaUsuarioComumECriptografaASenha() {
        when(userRepository.existsByEmail("ana@teste.com")).thenReturn(false);
        when(passwordEncoder.encode("senha1234")).thenReturn("hash-bcrypt");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse resposta = userService.registrar(new RegisterRequest("  Ana ", " ANA@teste.com ", "senha1234"));

        ArgumentCaptor<User> salvo = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(salvo.capture());
        assertThat(salvo.getValue().getRole()).isEqualTo(Role.USER);
        assertThat(salvo.getValue().getSenha()).isEqualTo("hash-bcrypt");
        assertThat(salvo.getValue().getEmail()).isEqualTo("ana@teste.com");
        assertThat(resposta.nome()).isEqualTo("Ana");
    }

    @Test
    void registrarComEmailRepetidoLancaConflito() {
        when(userRepository.existsByEmail("ana@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registrar(new RegisterRequest("Ana", "ana@teste.com", "senha1234")))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void usuarioComumNaoPodeCriarUsuario() {
        var request = new UserCreateRequest("Novo", "novo@teste.com", "senha1234", Role.ADMIN);

        assertThatThrownBy(() -> userService.criar(request, comum)).isInstanceOf(AccessDeniedException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void adminPodeCriarOutroAdmin() {
        when(passwordEncoder.encode("senha1234")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse resposta = userService.criar(
                new UserCreateRequest("Novo", "novo@teste.com", "senha1234", Role.ADMIN), admin);

        assertThat(resposta.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void usuarioComumNaoPodeVerDadosDeOutroUsuario() {
        assertThatThrownBy(() -> userService.buscar(99L, comum)).isInstanceOf(AccessDeniedException.class);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void usuarioComumPodeVerOsProprioDados() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "ana@teste.com", Role.USER)));

        assertThat(userService.buscar(2L, comum).email()).isEqualTo("ana@teste.com");
    }

    @Test
    void adminVeQualquerUsuarioEDaErroSeNaoExiste() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.buscar(99L, admin)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void usuarioComumNaoPodeMudarOPerfilNaEdicao() {
        User existente = usuario(2L, "ana@teste.com", Role.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(existente));
        when(userRepository.existsByEmailAndIdNot("ana@teste.com", 2L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse resposta = userService.atualizar(2L,
                new UserUpdateRequest("Ana Maria", "ana@teste.com", null, Role.ADMIN), comum);

        assertThat(resposta.role()).isEqualTo(Role.USER);
        assertThat(resposta.nome()).isEqualTo("Ana Maria");
    }

    @Test
    void edicaoSemSenhaMantemASenhaAtual() {
        User existente = usuario(2L, "ana@teste.com", Role.USER);
        existente.setSenha("hash-antigo");
        when(userRepository.findById(2L)).thenReturn(Optional.of(existente));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.atualizar(2L, new UserUpdateRequest("Ana", "ana@teste.com", "", null), comum);

        assertThat(existente.getSenha()).isEqualTo("hash-antigo");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void edicaoComEmailDeOutroUsuarioLancaConflito() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "ana@teste.com", Role.USER)));
        when(userRepository.existsByEmailAndIdNot("outro@teste.com", 2L)).thenReturn(true);

        assertThatThrownBy(() -> userService.atualizar(2L,
                new UserUpdateRequest("Ana", "outro@teste.com", null, null), comum))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void adminPodeMudarOPerfilDeOutroUsuario() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "ana@teste.com", Role.USER)));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse resposta = userService.atualizar(2L,
                new UserUpdateRequest("Ana", "ana@teste.com", null, Role.ADMIN), admin);

        assertThat(resposta.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void usuarioComumNaoPodeExcluirOutroUsuario() {
        assertThatThrownBy(() -> userService.excluir(99L, comum)).isInstanceOf(AccessDeniedException.class);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void adminExcluiUsuario() {
        User existente = usuario(2L, "ana@teste.com", Role.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(existente));

        userService.excluir(2L, admin);

        verify(userRepository).delete(existente);
    }

    @Test
    void somenteAdminListaUsuarios() {
        assertThatThrownBy(() -> userService.listar(PageRequest.of(0, 10), comum))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void listagemSemOrdenacaoUsaONomeComoPadrao() {
        when(userRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        userService.listar(PageRequest.of(0, 10), admin);

        ArgumentCaptor<org.springframework.data.domain.Pageable> pagina =
                ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(userRepository).findAll(pagina.capture());
        assertThat(pagina.getValue().getSort()).isEqualTo(Sort.by("nome"));
    }

    @Test
    void listagemComCampoInvalidoDeOrdenacaoLancaErro400() {
        var pagina = PageRequest.of(0, 10, Sort.by("senha"));

        assertThatThrownBy(() -> userService.listar(pagina, admin)).isInstanceOf(InvalidRequestException.class);
        verify(userRepository, never()).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    private User usuario(Long id, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setNome("Fulano");
        user.setEmail(email);
        user.setSenha("hash");
        user.setRole(role);
        return user;
    }
}
