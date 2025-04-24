package ua.com.rocketlv.service2reactive.custom;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;
import ua.com.rocketlv.service2reactive.exceptions.ErrorResponse;

@Component
@Order(-2) // High precedence to ensure it's called before DefaultErrorWebExceptionHandler
@Slf4j
public class SecurityExceptionWebHandler extends AbstractErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public SecurityExceptionWebHandler(ErrorAttributes errorAttributes,
                                       WebProperties webProperties,
                                       ApplicationContext applicationContext,
                                       ServerCodecConfigurer serverCodecConfigurer,
                                       ObjectMapper objectMapper) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        super.setMessageWriters(serverCodecConfigurer.getWriters());
        super.setMessageReaders(serverCodecConfigurer.getReaders());
        this.objectMapper = objectMapper;
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);
        log.error("Security exception handler caught: {}", error.getMessage());

        if (error instanceof AccessDeniedException) {
            return handleAccessDeniedException((AccessDeniedException) error);
        } else if (error instanceof AuthenticationException) {
            return handleAuthenticationException((AuthenticationException) error);
        }

        // Only handle security-related exceptions, let other handlers deal with the rest
        return Mono.error(error);
    }

    private Mono<ServerResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());

        return ServerResponse.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new ErrorResponse(
                        HttpStatus.FORBIDDEN.value(),
                        ex.getMessage() != null ? ex.getMessage() : "Access denied")));
    }

    private Mono<ServerResponse> handleAuthenticationException(AuthenticationException ex) {
        log.error("Authentication failed: {}", ex.getMessage());

        return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new ErrorResponse(
                        HttpStatus.UNAUTHORIZED.value(),
                        ex.getMessage() != null ? ex.getMessage() : "Authentication failed")));
    }
}