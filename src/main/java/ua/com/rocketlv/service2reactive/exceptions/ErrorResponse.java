package ua.com.rocketlv.service2reactive.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class ErrorResponse {
    private int status;
    private String message;
}
