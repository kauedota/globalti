package br.com.globalti.prova.service;

import br.com.globalti.prova.domain.Address;
import br.com.globalti.prova.domain.User;
import br.com.globalti.prova.dto.request.AddressRequest;
import br.com.globalti.prova.dto.response.AddressResponse;
import br.com.globalti.prova.dto.response.CepResponse;
import br.com.globalti.prova.exception.InvalidRequestException;
import br.com.globalti.prova.exception.ResourceNotFoundException;
import br.com.globalti.prova.mapper.AddressMapper;
import br.com.globalti.prova.repository.AddressRepository;
import br.com.globalti.prova.repository.UserRepository;
import br.com.globalti.prova.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Essa classe serve para as regras de negócio dos endereços.
 * Um endereço pertence a um usuário: ADMIN mexe em qualquer um,
 * USER só nos endereços dele.
 */
@Service
public class AddressService {

    private static final Set<String> CAMPOS_ORDENAVEIS = Set.of("id", "cidade", "cep", "logradouro");
    private static final Sort ORDENACAO_PADRAO = Sort.by("id");

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final CepService cepService;
    private final AddressMapper addressMapper;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository,
                          CepService cepService, AddressMapper addressMapper) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.cepService = cepService;
        this.addressMapper = addressMapper;
    }

    @Transactional(readOnly = true)
    public Page<AddressResponse> listarPorUsuario(Long userId, Pageable pageable, UserPrincipal solicitante) {
        garantirAcesso(userId, solicitante);
        buscarUsuario(userId);
        Pageable paginaValida = Paginacao.validar(pageable, CAMPOS_ORDENAVEIS, ORDENACAO_PADRAO);
        return addressRepository.findByUserId(userId, paginaValida).map(addressMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AddressResponse buscar(Long id, UserPrincipal solicitante) {
        Address address = buscarEntidade(id);
        garantirAcesso(address.getUser().getId(), solicitante);
        return addressMapper.toResponse(address);
    }

    @Transactional
    public AddressResponse criar(Long userId, AddressRequest request, UserPrincipal solicitante) {
        garantirAcesso(userId, solicitante);
        User dono = buscarUsuario(userId);

        Address address = new Address();
        address.setUser(dono);
        preencher(address, request);

        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse atualizar(Long id, AddressRequest request, UserPrincipal solicitante) {
        Address address = buscarEntidade(id);
        garantirAcesso(address.getUser().getId(), solicitante);

        preencher(address, request);
        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Transactional
    public void excluir(Long id, UserPrincipal solicitante) {
        Address address = buscarEntidade(id);
        garantirAcesso(address.getUser().getId(), solicitante);
        addressRepository.delete(address);
    }

    // Copia os dados do request para o endereço. Sempre consulto o ViaCEP para confirmar que o CEP existe;
    // o que o cliente não mandou (logradouro, bairro, cidade, estado), eu pego da resposta dele.
    private void preencher(Address address, AddressRequest request) {
        CepResponse cep = cepService.consultar(request.cep());

        address.setCep(cep.cep());
        address.setNumero(request.numero().trim());
        address.setComplemento(valorOuNulo(request.complemento()));
        address.setLogradouro(escolher(request.logradouro(), cep.logradouro()));
        address.setBairro(escolher(request.bairro(), cep.bairro()));
        address.setCidade(escolher(request.cidade(), cep.cidade()));
        address.setEstado(escolher(request.estado(), cep.estado()).toUpperCase());
    }

    // Usa o valor do cliente; se ele não mandou, usa o do ViaCEP.
    // CEP de cidade pequena às vezes vem sem logradouro e bairro, e aí o cliente precisa informar.
    private String escolher(String doCliente, String doViaCep) {
        String valor = valorOuNulo(doCliente) != null ? doCliente.trim() : valorOuNulo(doViaCep);
        if (valor == null) {
            throw new InvalidRequestException(
                    "Esse CEP não trouxe logradouro, bairro, cidade e estado completos. Preencha os campos que faltam");
        }
        return valor;
    }

    private String valorOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    // Mesma regra do UserService: ADMIN passa sempre, USER só no que é dele.
    private void garantirAcesso(Long idDoDono, UserPrincipal solicitante) {
        if (!solicitante.isAdmin() && !solicitante.getId().equals(idDoDono)) {
            throw new AccessDeniedException("Você só pode acessar os seus próprios endereços");
        }
    }

    private User buscarUsuario(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    private Address buscarEntidade(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado: " + id));
    }
}
