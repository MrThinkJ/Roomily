package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private String address;
    private String email;
    private String phone;
    private boolean isLandlord;
}
