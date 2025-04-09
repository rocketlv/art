package ua.com.rocketlv.service2reactive.exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(Long id) {
        super("User with ID " + id + " not found");
    }
}

