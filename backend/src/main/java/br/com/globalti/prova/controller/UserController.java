package br.com.globalti.prova.controller;

import br.com.globalti.prova.dto.request.UserCreateRequest;
import br.com.globalti.prova.dto.request.UserUpdateRequest;
import br.com.globalti.prova.dto.response.UserResponse;
import br.com.globalti.prova.security.UserPrincipal;
import br.com.globalti.prova.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Essa classe serve para expor as rotas de usuários.
 * Ela só recebe e responde HTTP; as regras de permissão estão no UserService.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuários")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Exemplo: GET /api/users?page=0&size=10&sort=nome,asc (só ADMIN)
    @GetMapping
    public Page<UserResponse> listar(@Parameter(hidden = true) Pageable pageable,
                                     @AuthenticationPrincipal UserPrincipal logado) {
        return userService.listar(pageable, logado);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse criar(@Valid @RequestBody UserCreateRequest request,
                              @AuthenticationPrincipal UserPrincipal logado) {
        return userService.criar(request, logado);
    }

    // Devolve o usuário dono do token. O front usa para saber quem está logado.
    @GetMapping("/me")
    public UserResponse eu(@AuthenticationPrincipal UserPrincipal logado) {
        return userService.buscar(logado.getId(), logado);
    }

    @GetMapping("/{id}")
    public UserResponse buscar(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal logado) {
        return userService.buscar(id, logado);
    }

    @PutMapping("/{id}")
    public UserResponse atualizar(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request,
                                  @AuthenticationPrincipal UserPrincipal logado) {
        return userService.atualizar(id, request, logado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal logado) {
        userService.excluir(id, logado);
    }
}
