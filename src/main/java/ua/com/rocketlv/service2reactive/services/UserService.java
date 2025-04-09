package ua.com.rocketlv.service2reactive.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ua.com.rocketlv.service2reactive.exceptions.ObjectNotFoundException;
import ua.com.rocketlv.service2reactive.exceptions.UserNotFoundException;
import ua.com.rocketlv.service2reactive.dto.UserDto;
import ua.com.rocketlv.service2reactive.repo.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    final UserMapper userMapper;

    private final UserRepository userRepository;

    public Mono<UserDto> getUserById(Long id) {
        return userRepository.findById(id).map(userMapper::mapToUserDto)
                .switchIfEmpty(Mono.error(new UserNotFoundException(id)));

    }

    public Flux<UserDto> getAllUsers() {
        return userRepository.findAll().map(userMapper::mapToUserDto);
    }

    public Flux<UserDto> getUsersByCity(String city) {
        return userRepository.findByCity(city).map(userMapper::mapToUserDto).switchIfEmpty(
                Mono.error(new ObjectNotFoundException(city))
        );
    }

}
