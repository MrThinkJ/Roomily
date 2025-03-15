package com.c2se.roomily.controller;

import com.c2se.roomily.payload.response.UserInfoResponse;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public abstract class BaseController {
    protected UserInfoResponse getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return UserInfoResponse.builder()
                .id(userDetails.getId()).build();
    }
}
