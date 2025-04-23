package ua.com.rocketlv.service2reactive;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;
import ua.com.rocketlv.service2reactive.exceptions.ErrorResponse;
import ua.com.rocketlv.service2reactive.exceptions.ObjectNotFoundException;
import ua.com.rocketlv.service2reactive.exceptions.UserNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalErrorHandler {


    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public Mono<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        System.out.println("UserNotFoundException: " + ex.getMessage());
        return Mono.just(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public  Mono<ErrorResponse> handleObjectNotFound(ObjectNotFoundException ex) {
        return Mono.just(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public  Mono<ErrorResponse> handleAllExceptions(Exception ex) {
        return Mono.just(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalAccessException.class)
    public Mono<ErrorResponse> handleIllegalAccessException(IllegalAccessException ex) {
        return Mono.just(ex.getMessage())
                .map(message -> new ErrorResponse(HttpStatus.FORBIDDEN.value(), message));
    }}

