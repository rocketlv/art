package ua.com.rocketlv.service2reactive.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import ua.com.rocketlv.service2reactive.dao.User;
import ua.com.rocketlv.service2reactive.dao.Userlog;
import ua.com.rocketlv.service2reactive.repo.UserRepository;
import ua.com.rocketlv.service2reactive.repo.UserlogRepository;

@Configuration
public class AppConfig {


    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, UserlogRepository userlogRepository) {
        return args -> {
            // Clear existing data
            Flux<User> users = Flux.just(
                    new User(null, "John Doe", 30, "New York", "Software developer","rocketlv@gmail.com"),
                    new User(null, "Jane Smith", 25, "London", "Data scientist","transpo@gmail.com"),
                    new User(null, "Mike Johnson", 35, "Berlin", "Product manager","goal@gmail.com"),
                    new User(null, "Юрій Метопенко", 35, "Berlin", "Tester manager","rafl@gmail.com"),
                    new User(null, "Андрій Пилипенко", 47, "Berlin", "Software developer","most@gmail.com")
            );
            userRepository.deleteAll().thenMany(
                            users.flatMap(userRepository::save)
                    ).thenMany(userRepository.findAll())
                    .subscribe(
                            user -> {
                                Flux<Userlog> userlogs = Flux.just(new Userlog(null, user.getId(), 33, "Log message 1"),
                                        new Userlog(null, user.getId(), 33, "Log message 2"),
                                        new Userlog(null, user.getId(), 44, "Log message 3"));
                                System.out.println("Inserted: " + user);
                                userlogs.flatMap(userlog -> {
                                            System.out.println("Inserted log: " + userlog);
                                            return userlogRepository.save(userlog);
                                        }
                                ).subscribe(
                                        log -> System.out.println("Log saved: " + log),
                                        error -> System.err.println("Error saving log: " + error)
                                );
                            },
                            error -> System.err.println("Error: " + error),
                            () -> System.out.println("Initialization completed")
                    );
        };
    }
}
