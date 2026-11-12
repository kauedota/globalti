package br.com.globalti.prova.mapper;

import br.com.globalti.prova.domain.Address;
import br.com.globalti.prova.dto.response.AddressResponse;
import org.springframework.stereotype.Component;

/**
 * Essa classe serve para converter Address (entidade) em AddressResponse.
 */
@Component
public class AddressMapper {

    public AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getLogradouro(),
                address.getNumero(),
                address.getComplemento(),
                address.getBairro(),
                address.getCidade(),
                address.getEstado(),
                address.getCep(),
                address.getUser().getId()
        );
    }
}
