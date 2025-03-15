package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String username;
    private String userId;
    private Set<String> role;
}
