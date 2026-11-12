package br.com.globalti.prova.service;

import br.com.globalti.prova.domain.Address;
import br.com.globalti.prova.domain.Role;
import br.com.globalti.prova.domain.User;
import br.com.globalti.prova.dto.request.AddressRequest;
import br.com.globalti.prova.dto.response.AddressResponse;
import br.com.globalti.prova.dto.response.CepResponse;
import br.com.globalti.prova.exception.InvalidCepException;
import br.com.globalti.prova.exception.InvalidRequestException;
import br.com.globalti.prova.exception.ResourceNotFoundException;
import br.com.globalti.prova.mapper.AddressMapper;
import br.com.globalti.prova.repository.AddressRepository;
import br.com.globalti.prova.repository.UserRepository;
import br.com.globalti.prova.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Essa classe serve para testar as regras do AddressService sem banco e sem internet.
 */
@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CepService cepService;

    private AddressService addressService;

    private final UserPrincipal admin = new UserPrincipal(1L, "admin@teste.com", "hash", Role.ADMIN);
    private final UserPrincipal ana = new UserPrincipal(2L, "ana@teste.com", "hash", Role.USER);

    private final CepResponse cepPaulista = new CepResponse(
            "01310100", "Avenida Paulista", "", "Bela Vista", "São Paulo", "SP");

    @BeforeEach
    void preparar() {
        addressService = new AddressService(addressRepository, userRepository, cepService, new AddressMapper());
    }

    @Test
    void criarPreencheEnderecoComOsDadosDoViaCepQuandoOClienteNaoManda() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(usuario(2L)));
        when(cepService.consultar("01310-100")).thenReturn(cepPaulista);
        when(addressRepository.save(any(Address.class))).thenAnswer(i -> i.getArgument(0));

        AddressResponse resposta = addressService.criar(2L,
                new AddressRequest("01310-100", "1000", null, null, null, null, null), ana);

        assertThat(resposta.cep()).isEqualTo("01310100");
        assertThat(resposta.logradouro()).isEqualTo("Avenida Paulista");
        assertThat(resposta.bairro()).isEqualTo("Bela Vista");
        assertThat(resposta.cidade()).isEqualTo("São Paulo");
        assertThat(resposta.estado()).isEqualTo("SP");
        assertThat(resposta.userId()).isEqualTo(2L);
    }

    @Test
    void criarMantemOQueOClienteMandouEmVezDoViaCep() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(usuario(2L)));
        when(cepService.consultar("01310100")).thenReturn(cepPaulista);
        when(addressRepository.save(any(Address.class))).thenAnswer(i -> i.getArgument(0));

        AddressResponse resposta = addressService.criar(2L,
                new AddressRequest("01310100", "1000", "Sala 2", "Av. Paulista", "Centro", "Sampa", "sp"), ana);

        assertThat(resposta.logradouro()).isEqualTo("Av. Paulista");
        assertThat(resposta.bairro()).isEqualTo("Centro");
        assertThat(resposta.cidade()).isEqualTo("Sampa");
        assertThat(resposta.estado()).isEqualTo("SP");
        assertThat(resposta.complemento()).isEqualTo("Sala 2");
    }

    @Test
    void criarExigeOsCamposQueOViaCepNaoTrouxe() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(usuario(2L)));
        // CEP "geral" de cidade pequena: vem sem logradouro e sem bairro.
        when(cepService.consultar("12345678")).thenReturn(
                new CepResponse("12345678", "", "", "", "Cidadezinha", "MG"));

        assertThatThrownBy(() -> addressService.criar(2L,
                new AddressRequest("12345678", "10", null, null, null, null, null), ana))
                .isInstanceOf(InvalidRequestException.class);
        verify(addressRepository, never()).save(any());
    }

    @Test
    void criarComCepInexistentePropagaOErro() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(usuario(2L)));
        when(cepService.consultar("99999999")).thenThrow(new InvalidCepException("CEP não encontrado"));

        assertThatThrownBy(() -> addressService.criar(2L,
                new AddressRequest("99999999", "10", null, null, null, null, null), ana))
                .isInstanceOf(InvalidCepException.class);
    }

    @Test
    void usuarioComumNaoCriaEnderecoParaOutroUsuario() {
        assertThatThrownBy(() -> addressService.criar(3L,
                new AddressRequest("01310100", "10", null, null, null, null, null), ana))
                .isInstanceOf(AccessDeniedException.class);
        verify(cepService, never()).consultar(any());
    }

    @Test
    void adminCriaEnderecoParaQualquerUsuarioMasUsuarioInexistenteDa404() {
        when(userRepository.findById(50L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.criar(50L,
                new AddressRequest("01310100", "10", null, null, null, null, null), admin))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void usuarioComumNaoVeEnderecoDeOutroUsuario() {
        when(addressRepository.findById(7L)).thenReturn(Optional.of(endereco(7L, usuario(3L))));

        assertThatThrownBy(() -> addressService.buscar(7L, ana)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adminVeEnderecoDeQualquerUsuario() {
        when(addressRepository.findById(7L)).thenReturn(Optional.of(endereco(7L, usuario(3L))));

        assertThat(addressService.buscar(7L, admin).userId()).isEqualTo(3L);
    }

    @Test
    void usuarioComumNaoExcluiEnderecoDeOutroUsuario() {
        when(addressRepository.findById(7L)).thenReturn(Optional.of(endereco(7L, usuario(3L))));

        assertThatThrownBy(() -> addressService.excluir(7L, ana)).isInstanceOf(AccessDeniedException.class);
        verify(addressRepository, never()).delete(any());
    }

    @Test
    void donoExcluiOProprioEndereco() {
        Address proprio = endereco(8L, usuario(2L));
        when(addressRepository.findById(8L)).thenReturn(Optional.of(proprio));

        addressService.excluir(8L, ana);

        verify(addressRepository).delete(proprio);
    }

    @Test
    void atualizarReaproveitaOViaCepParaPreencherOsCampos() {
        Address existente = endereco(8L, usuario(2L));
        when(addressRepository.findById(8L)).thenReturn(Optional.of(existente));
        when(cepService.consultar("01310100")).thenReturn(cepPaulista);
        when(addressRepository.save(any(Address.class))).thenAnswer(i -> i.getArgument(0));

        AddressResponse resposta = addressService.atualizar(8L,
                new AddressRequest("01310100", "2000", null, null, null, null, null), ana);

        assertThat(resposta.numero()).isEqualTo("2000");
        assertThat(resposta.logradouro()).isEqualTo("Avenida Paulista");
    }

    @Test
    void enderecoInexistenteDa404() {
        when(addressRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.buscar(404L, admin)).isInstanceOf(ResourceNotFoundException.class);
    }

    private User usuario(Long id) {
        User user = new User();
        user.setId(id);
        user.setRole(Role.USER);
        return user;
    }

    private Address endereco(Long id, User dono) {
        Address address = new Address();
        address.setId(id);
        address.setUser(dono);
        address.setCep("01310100");
        address.setNumero("1");
        address.setLogradouro("Rua A");
        address.setBairro("Centro");
        address.setCidade("São Paulo");
        address.setEstado("SP");
        return address;
    }
}
