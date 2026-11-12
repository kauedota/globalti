package br.com.globalti.prova.service;

import br.com.globalti.prova.client.ViaCepClient;
import br.com.globalti.prova.client.ViaCepResponse;
import br.com.globalti.prova.dto.response.CepResponse;
import br.com.globalti.prova.exception.CepServiceUnavailableException;
import br.com.globalti.prova.exception.InvalidCepException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Essa classe serve para testar a validação e a consulta de CEP, com o ViaCEP mockado.
 */
@ExtendWith(MockitoExtension.class)
class CepServiceTest {

    @Mock
    private ViaCepClient viaCepClient;
    @InjectMocks
    private CepService cepService;

    @Test
    void normalizaCepComHifen() {
        assertThat(cepService.normalizar("01310-100")).isEqualTo("01310100");
    }

    @Test
    void mantemCepSemHifen() {
        assertThat(cepService.normalizar("01310100")).isEqualTo("01310100");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123", "0131010", "013101000", "abcde-fgh", "01310_100", "0131-0100"})
    void rejeitaCepMalFormado(String cep) {
        assertThatThrownBy(() -> cepService.normalizar(cep)).isInstanceOf(InvalidCepException.class);
    }

    @Test
    void rejeitaCepNulo() {
        assertThatThrownBy(() -> cepService.normalizar(null)).isInstanceOf(InvalidCepException.class);
    }

    @Test
    void consultaOViaCepComOCepNormalizadoEConverteAResposta() {
        when(viaCepClient.buscar("01310100")).thenReturn(new ViaCepResponse(
                "01310-100", "Avenida Paulista", "de 612 a 1510 - lado par", "Bela Vista", "São Paulo", "SP", null));

        CepResponse resposta = cepService.consultar("01310-100");

        assertThat(resposta.cep()).isEqualTo("01310100");
        assertThat(resposta.cidade()).isEqualTo("São Paulo");
        assertThat(resposta.estado()).isEqualTo("SP");
    }

    @Test
    void cepQueOViaCepNaoConheceVira400() {
        when(viaCepClient.buscar("99999999")).thenReturn(new ViaCepResponse(null, null, null, null, null, null, true));

        assertThatThrownBy(() -> cepService.consultar("99999999"))
                .isInstanceOf(InvalidCepException.class)
                .hasMessageContaining("99999999");
    }

    @Test
    void naoChamaOViaCepQuandoOFormatoEInvalido() {
        assertThatThrownBy(() -> cepService.consultar("123")).isInstanceOf(InvalidCepException.class);
        verify(viaCepClient, never()).buscar(any());
    }

    @Test
    void propagaOErroQuandoOViaCepEstaForaDoAr() {
        when(viaCepClient.buscar("01310100"))
                .thenThrow(new CepServiceUnavailableException(new RuntimeException("timeout")));

        assertThatThrownBy(() -> cepService.consultar("01310100"))
                .isInstanceOf(CepServiceUnavailableException.class);
    }
}
