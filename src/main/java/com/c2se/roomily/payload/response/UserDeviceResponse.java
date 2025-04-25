package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDeviceResponse {
    private String id;
    private String fcmToken;
    private String deviceType;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;
}
