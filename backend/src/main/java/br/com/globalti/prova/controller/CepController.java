package br.com.globalti.prova.controller;

import br.com.globalti.prova.dto.response.CepResponse;
import br.com.globalti.prova.service.CepService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Essa classe serve para o front consultar um CEP (ele usa para preencher o formulário de endereço).
 */
@RestController
@RequestMapping("/api/cep")
@Tag(name = "CEP")
public class CepController {

    private final CepService cepService;

    public CepController(CepService cepService) {
        this.cepService = cepService;
    }

    @GetMapping("/{cep}")
    public CepResponse consultar(@PathVariable String cep) {
        return cepService.consultar(cep);
    }
}
