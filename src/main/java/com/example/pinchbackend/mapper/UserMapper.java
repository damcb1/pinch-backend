package com.example.pinchbackend.mapper;

import com.example.pinchbackend.dto.response.UserResponse;
import com.example.pinchbackend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getEmail());
    }
}