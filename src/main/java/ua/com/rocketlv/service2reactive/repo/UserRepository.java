package ua.com.rocketlv.service2reactive.repo;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import ua.com.rocketlv.service2reactive.dao.User;

public interface UserRepository extends R2dbcRepository<User, Long> {
    Flux<User> findByAge(Integer age);
    Flux<User> findByCity(String city);
}
