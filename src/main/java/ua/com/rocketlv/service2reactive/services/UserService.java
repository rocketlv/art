package ua.com.rocketlv.service2reactive.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ua.com.rocketlv.service2reactive.exceptions.ObjectNotFoundException;
import ua.com.rocketlv.service2reactive.exceptions.UserNotFoundException;
import ua.com.rocketlv.service2reactive.dto.UserDto;
import ua.com.rocketlv.service2reactive.repo.UserRepository;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserService {
    final UserMapper userMapper;

    private final UserRepository userRepository;

    public Mono<UserDto> getUserById(Long id) {
        var s =  userRepository.findById(id).map(userMapper::mapToUserDto).filter(userDto -> userDto.getId().equals(id))
                .switchIfEmpty(Mono.error(new UserNotFoundException(id)));
        s.subscribe(res-> System.out.println("User found: " + res));
        return s;

    }

    public Flux<UserDto> getAllUsers() {
        var u =  userRepository.findAll().map(userMapper::mapToUserDto).delayElements(Duration.ofMillis(0));
        u.subscribe(res-> System.out.println("All users: " + res));
        return u;
    }

    public Flux<UserDto> getUsersByCity(String city) {
        return userRepository.findByCity(city).map(userMapper::mapToUserDto).switchIfEmpty(
                Mono.error(new ObjectNotFoundException(city))
        );
    }

}
