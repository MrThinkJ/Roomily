package com.c2se.roomily.controller;

import com.c2se.roomily.payload.response.UserInfoResponse;
import com.c2se.roomily.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseController {
    @Autowired
    UserRepository userRepository;
    protected UserInfoResponse getUserInfo() {
        String id = userRepository.findByUsername("admin").getId();
        return UserInfoResponse.builder()
                .id(id).build();
    }
}
