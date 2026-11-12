package br.com.globalti.prova.service;

import br.com.globalti.prova.client.ViaCepClient;
import br.com.globalti.prova.client.ViaCepResponse;
import br.com.globalti.prova.dto.response.CepResponse;
import br.com.globalti.prova.exception.InvalidCepException;
import org.springframework.stereotype.Service;

/**
 * Essa classe serve para validar, normalizar e consultar CEPs.
 */
@Service
public class CepService {

    private final ViaCepClient viaCepClient;

    public CepService(ViaCepClient viaCepClient) {
        this.viaCepClient = viaCepClient;
    }

    // Aceita "01310-100" ou "01310100" e devolve sempre só os 8 dígitos.
    // É assim que o CEP vai para o banco.
    public String normalizar(String cep) {
        if (cep == null || !cep.matches("\\d{5}-?\\d{3}")) {
            throw new InvalidCepException("CEP inválido: use 8 dígitos, com ou sem hífen");
        }
        return cep.replace("-", "");
    }

    // Consulta o ViaCEP. Se o CEP tem o formato certo mas não existe, o ViaCEP devolve {"erro": true}
    // e eu respondo 400 com uma mensagem clara.
    public CepResponse consultar(String cep) {
        String cepNormalizado = normalizar(cep);
        ViaCepResponse resposta = viaCepClient.buscar(cepNormalizado);

        if (resposta.naoEncontrado()) {
            throw new InvalidCepException("CEP não encontrado: " + cepNormalizado);
        }

        return new CepResponse(
                cepNormalizado,
                resposta.logradouro(),
                resposta.complemento(),
                resposta.bairro(),
                resposta.localidade(),
                resposta.uf()
        );
    }
}
