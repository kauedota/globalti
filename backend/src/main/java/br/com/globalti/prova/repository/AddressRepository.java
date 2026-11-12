package br.com.globalti.prova.repository;

import br.com.globalti.prova.domain.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Essa interface serve para acessar a tabela de endereços no banco.
 */
public interface AddressRepository extends JpaRepository<Address, Long> {

    // Lista só os endereços de um usuário, já paginados.
    Page<Address> findByUserId(Long userId, Pageable pageable);

    long countByUserId(Long userId);
}
