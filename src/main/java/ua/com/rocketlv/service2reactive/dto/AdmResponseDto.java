package ua.com.rocketlv.service2reactive.dto;

import lombok.*;

@RequiredArgsConstructor
@Builder
@Data
@EqualsAndHashCode
@ToString
public class AdmResponseDto {
    private final Integer idPmu;
    private final String fullnamePmu;
    private final String emailPmu;
}
