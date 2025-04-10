package ua.com.rocketlv.service2reactive.repo;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import ua.com.rocketlv.service2reactive.dao.Userlog;

public interface UserlogRepository extends R2dbcRepository<Userlog, Long> {
    // Custom query methods can be defined here if needed
    // For example, you can add methods to find logs by userId or logTimestamp
}
