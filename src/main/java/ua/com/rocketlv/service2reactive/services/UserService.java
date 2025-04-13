package ua.com.rocketlv.service2reactive.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ua.com.rocketlv.service2reactive.dao.User;
import ua.com.rocketlv.service2reactive.dao.Userlog;
import ua.com.rocketlv.service2reactive.dto.UserDto;
import ua.com.rocketlv.service2reactive.exceptions.ObjectNotFoundException;
import ua.com.rocketlv.service2reactive.exceptions.UserNotFoundException;
import ua.com.rocketlv.service2reactive.repo.UserRepository;
import ua.com.rocketlv.service2reactive.repo.UserlogRepository;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    final UserMapper userMapper;

    private final UserRepository userRepository;
    private final UserlogRepository userlogRepository;

    public Mono<UserDto> getUserById(Long id) {
        var s = userRepository.findById(id).map(userMapper::mapToUserDto).filter(userDto -> userDto.getId().equals(id))
                .switchIfEmpty(Mono.error(new UserNotFoundException(id)));
        s.subscribe(res -> System.out.println("User found: " + res));
        return s;

    }

    public Flux<UserDto> getAllUsers() {
        var u = userRepository.findAll().map(userMapper::mapToUserDto).delayElements(Duration.ofMillis(0));
        u.subscribe(res -> System.out.println("All users: " + res));
        return u;
    }

    public Flux<UserDto> getUsersByCity(String city) {
        return userRepository.findByCity(city).map(userMapper::mapToUserDto).switchIfEmpty(
                Mono.error(new ObjectNotFoundException(city))
        );
    }

//    public Mono<UserDto> getUserWithLogs(Long id) {
//        return userRepository.findById(id)
//                .flatMap(user -> userlogRepository.findByUserId(id)
//                        .collectList()
//                        .map(logs -> userMapper.mapToUserDtoWithLogs(user, logs)))
//                .switchIfEmpty(Mono.error(new UserNotFoundException(id)));
//    }

    public Mono<UserDto> getUserWithLogs(Long id) {

        var userDtoMono = userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
                .flatMap(user ->
                        userlogRepository.findByUserId(user.getId())
                                .collectList()
                                .map(logs -> userMapper.mapToUserDtoWithLogs(user, logs)));
        return userDtoMono;
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public Mono<UserDto> createUserWithLogs(UserDto userDto, List<Userlog> userlogs) {
        return Mono.just(userDto)
                .filter(dto -> dto != null)
                .map(userMapper::mapToUser)
                .flatMap(user -> userRepository.save(user)
                        .switchIfEmpty(Mono.error(new RuntimeException("User creation Error !")))
                        .flatMap(createduser -> {
                            Flux<Userlog> userlogFlux = Flux.fromIterable(userlogs);
                            return userlogFlux.filter(listoflogs -> listoflogs != null)
                                    .flatMap(userlog -> {
                                        userlog.setUserId(createduser.getId());
                                        return userlogRepository.save(userlog)
                                                .switchIfEmpty(Mono.error(new RuntimeException("User log creation Error !")));
                                    }).then(Mono.just(createduser).map(userMapper::mapToUserDto));
                        })
                );

    }
}
