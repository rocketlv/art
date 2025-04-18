package ua.com.rocketlv.service2reactive;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Base64;

@Configuration
public class WebClientConfig {
    @Value("${web-client-endpoints.epm-api.url}")
    private String epmApiUrl;

    @Bean
    public WebClient webClient() {
        System.out.printf("EPM API URL: %s%n", epmApiUrl);
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString("rocketlv:EcoSystem1978".getBytes()))
                .baseUrl(epmApiUrl)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().responseTimeout(Duration.ofSeconds(10))
                )).build();
    }


}
