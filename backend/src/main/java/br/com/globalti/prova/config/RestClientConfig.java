package br.com.globalti.prova.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Essa classe serve para criar o RestClient usado para falar com o ViaCEP,
 * já com os tempos limite configurados (para a API não ficar presa se o ViaCEP travar).
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient viaCepRestClient(
            @Value("${app.viacep.base-url}") String baseUrl,
            @Value("${app.viacep.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${app.viacep.read-timeout-ms}") int readTimeoutMs) {

        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(connectTimeoutMs);
        fabrica.setReadTimeout(readTimeoutMs);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(fabrica)
                .build();
    }
}
