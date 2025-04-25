package com.c2se.roomily.payload.internal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PushNotificationDto {
    private String title;
    private String message;
    private String topic;
    private String token;
}
