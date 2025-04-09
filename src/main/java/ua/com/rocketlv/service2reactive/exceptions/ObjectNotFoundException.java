package ua.com.rocketlv.service2reactive.exceptions;

public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException(String value) {
        super("Object with name" + value + " not found");
    }
}
