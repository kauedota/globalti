package br.com.globalti.prova.service;

import br.com.globalti.prova.exception.InvalidRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * Essa classe serve para validar a ordenação pedida pelo cliente.
 * Só deixo ordenar pelos campos permitidos; qualquer outro vira erro 400
 * (se eu passasse direto para o JPA, daria erro 500).
 */
final class Paginacao {

    private Paginacao() {
    }

    static Pageable validar(Pageable pageable, Set<String> camposPermitidos, Sort ordenacaoPadrao) {
        // Se o cliente não pediu ordenação, uso a padrão.
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), ordenacaoPadrao);
        }

        for (Sort.Order ordem : pageable.getSort()) {
            if (!camposPermitidos.contains(ordem.getProperty())) {
                throw new InvalidRequestException("Não é possível ordenar por '" + ordem.getProperty()
                        + "'. Campos permitidos: " + String.join(", ", camposPermitidos));
            }
        }
        return pageable;
    }
}
