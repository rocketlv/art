package ua.com.rocketlv.service2reactive.exceptions;

public class UserLogCreationException extends RuntimeException{
    public UserLogCreationException() {
        super("Can`t create new user log");
    }
}

