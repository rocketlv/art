package ua.com.rocketlv.service2reactive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private Integer age;
    private String city;
    private String description;
}
