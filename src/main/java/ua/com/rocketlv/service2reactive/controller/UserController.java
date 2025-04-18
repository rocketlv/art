package ua.com.rocketlv.service2reactive.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ua.com.rocketlv.service2reactive.dto.AdmResponseDto;
import ua.com.rocketlv.service2reactive.exceptions.UserNotFoundException;
import ua.com.rocketlv.service2reactive.dto.UserDto;
import ua.com.rocketlv.service2reactive.services.RemoteApiService;
import ua.com.rocketlv.service2reactive.services.UserService;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")

public class UserController {
    private final UserService userService;
    private final RemoteApiService remoteApiService;


    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<UserDto> getAllUsers() {
        return userService.getAllUsers().delayElements(Duration.ofSeconds(0));
    }
    @CrossOrigin("*")
    @GetMapping("/{id}")
    public Mono<UserDto> getUserById(@PathVariable Long id) {
        return userService.getUserWithLogs(id);
    }

    @GetMapping("/city/{city}")
    public Flux<UserDto> getUsersByCity(@PathVariable String city) {
        return userService.getUsersByCity(city);
    }

    @GetMapping("/remote")
    public Flux<AdmResponseDto> getRemoteUsers() {
        return remoteApiService.getRemoteApiService();
    }
}
