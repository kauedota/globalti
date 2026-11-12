package br.com.globalti.prova.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * Essa classe serve para configurar a paginação da API:
 * - o formato JSON das páginas: {"content": [...], "page": {"size", "number", "totalElements", "totalPages"}};
 * - o tamanho padrão (10) e o máximo (100) de itens por página.
 * Como o @EnableSpringDataWebSupport desliga a configuração automática do Spring Boot,
 * os limites precisam ser definidos aqui.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class PaginacaoConfig {

    private static final int TAMANHO_PADRAO = 10;
    private static final int TAMANHO_MAXIMO = 100;

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer limitesDePagina() {
        return resolver -> {
            resolver.setFallbackPageable(PageRequest.of(0, TAMANHO_PADRAO));
            resolver.setMaxPageSize(TAMANHO_MAXIMO);
        };
    }
}
