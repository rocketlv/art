package ua.com.rocketlv.service2reactive.services;

import org.springframework.stereotype.Service;
import ua.com.rocketlv.service2reactive.dao.User;
import ua.com.rocketlv.service2reactive.dao.Userlog;
import ua.com.rocketlv.service2reactive.dto.UserDto;

import java.util.List;

@Service
public class UserMapper {
    public ua.com.rocketlv.service2reactive.dao.User mapToUser(ua.com.rocketlv.service2reactive.dto.UserDto userDto) {
        ua.com.rocketlv.service2reactive.dao.User user = new ua.com.rocketlv.service2reactive.dao.User();
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user.setAge(userDto.getAge());
        user.setCity(userDto.getCity());
        user.setDescription(userDto.getDescription());
        return user;
    }

    public ua.com.rocketlv.service2reactive.dto.UserDto mapToUserDto(ua.com.rocketlv.service2reactive.dao.User user) {
        ua.com.rocketlv.service2reactive.dto.UserDto userDto = new ua.com.rocketlv.service2reactive.dto.UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setAge(user.getAge());
        userDto.setCity(user.getCity());
        userDto.setDescription(user.getDescription());
        return userDto;
    }

    public UserDto mapToUserDtoWithLogs(User user, List<Userlog> logs) {
        UserDto userDto = new ua.com.rocketlv.service2reactive.dto.UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setAge(user.getAge());
        userDto.setCity(user.getCity());
        userDto.setDescription(user.getDescription());
        userDto.setLogs(logs);
        return userDto;
    }
}
