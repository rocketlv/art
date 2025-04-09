package ua.com.rocketlv.service2reactive.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ua.com.rocketlv.service2reactive.exceptions.UserNotFoundException;
import ua.com.rocketlv.service2reactive.dto.UserDto;
import ua.com.rocketlv.service2reactive.services.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")

public class UserController {
    private final UserService userService;



    @GetMapping
    public Flux<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }
    @CrossOrigin("*")
    @GetMapping("/{id}")
    public Mono<UserDto> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/city/{city}")
    public Flux<UserDto> getUsersByCity(@PathVariable String city) {
        return userService.getUsersByCity(city);
    }
}
