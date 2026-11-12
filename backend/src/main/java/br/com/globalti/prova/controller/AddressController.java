package br.com.globalti.prova.controller;

import br.com.globalti.prova.dto.request.AddressRequest;
import br.com.globalti.prova.dto.response.AddressResponse;
import br.com.globalti.prova.security.UserPrincipal;
import br.com.globalti.prova.service.AddressService;
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
 * Essa classe serve para expor as rotas de endereços.
 * Listar e criar ficam dentro do usuário (/users/{userId}/addresses);
 * ver, editar e excluir usam o id do próprio endereço (/addresses/{id}).
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Endereços")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/users/{userId}/addresses")
    public Page<AddressResponse> listar(@PathVariable Long userId,
                                        @Parameter(hidden = true) Pageable pageable,
                                        @AuthenticationPrincipal UserPrincipal logado) {
        return addressService.listarPorUsuario(userId, pageable, logado);
    }

    @PostMapping("/users/{userId}/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse criar(@PathVariable Long userId, @Valid @RequestBody AddressRequest request,
                                 @AuthenticationPrincipal UserPrincipal logado) {
        return addressService.criar(userId, request, logado);
    }

    @GetMapping("/addresses/{id}")
    public AddressResponse buscar(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal logado) {
        return addressService.buscar(id, logado);
    }

    @PutMapping("/addresses/{id}")
    public AddressResponse atualizar(@PathVariable Long id, @Valid @RequestBody AddressRequest request,
                                     @AuthenticationPrincipal UserPrincipal logado) {
        return addressService.atualizar(id, request, logado);
    }

    @DeleteMapping("/addresses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal logado) {
        addressService.excluir(id, logado);
    }
}
