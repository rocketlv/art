package ua.com.rocketlv.service2reactive.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import ua.com.rocketlv.service2reactive.dao.User;
import ua.com.rocketlv.service2reactive.repo.UserRepository;

@Configuration
public class AppConfig {


    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            // Clear existing data
            Flux<User> users = Flux.just(
                    new User(null, "John Doe", 30, "New York", "Software developer"),
                    new User(null, "Jane Smith", 25, "London", "Data scientist"),
                    new User(null, "Mike Johnson", 35, "Berlin", "Product manager")
            );

            userRepository.deleteAll().thenMany(
                            users.flatMap(userRepository::save)

                    ).thenMany(userRepository.findAll())
                    .subscribe(
                            user -> System.out.println("Inserted: " + user),
                            error -> System.err.println("Error: " + error),
                            () -> System.out.println("Initialization completed")
                    );


        };
    }
}
