package ua.com.rocketlv.service2reactive.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ua.com.rocketlv.service2reactive.dto.AdmResponseDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoteApiService {

    private final WebClient webClient;

    public Flux<AdmResponseDto> getRemoteApiService() {
        var result =  webClient.get().uri("/pmusers")
                .retrieve()
                .onStatus(httpStatus -> httpStatus.is4xxClientError() || httpStatus.is5xxServerError(),
                        clientResponse -> {
                            // Log the error or handle it as needed
                            log.error(clientResponse.statusCode().toString());
                            return Mono.error(new RuntimeException("Resource loading error !"));
                        }
                )
                .bodyToFlux(AdmResponseDto.class)
                .doOnNext(val-> log.info("Received response: {}", val))
                .take(5).filter(val -> val.getIdPmu()!=489).map(val->
                        AdmResponseDto.builder()
                                .idPmu(val.getIdPmu())
                                .fullnamePmu(val.getFullnamePmu()+" - "+val.getIdPmu())
                                .emailPmu(val.getEmailPmu())
                                .build()
                );            return result;

    }
}
