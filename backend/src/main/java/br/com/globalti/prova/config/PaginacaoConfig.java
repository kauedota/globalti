package br.com.globalti.prova.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * Essa classe serve para fixar o formato JSON das páginas:
 * {"content": [...], "page": {"size", "number", "totalElements", "totalPages"}}.
 * Sem isso o Spring serializa o objeto interno de página, que pode mudar entre versões.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class PaginacaoConfig {
}
