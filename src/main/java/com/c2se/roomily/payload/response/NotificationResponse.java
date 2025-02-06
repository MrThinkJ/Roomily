package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private String id;
    private String header;
    private String body;
    private Boolean isRead;
    private String type;
    private String createdAt;
    private String userId;
}
