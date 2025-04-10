package ua.com.rocketlv.service2reactive.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("userlog")
public class Userlog {
    @Id
    private Long id;
    private Long userId;
    private Integer operationsCount;
    private String description;
}