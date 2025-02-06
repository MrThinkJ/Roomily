package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateNotificationRequest {
    private String header;
    private String body;
    private String userId;
    private String type;
}
