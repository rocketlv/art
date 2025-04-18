package ua.com.rocketlv.service2reactive.exceptions;

public class UserCreationException extends RuntimeException{
    public UserCreationException() {
        super("Can`t create new user ");
    }
}

