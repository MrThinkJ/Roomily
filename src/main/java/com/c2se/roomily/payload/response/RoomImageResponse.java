package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoomImageResponse {
    private String id;
    private String name;
    private String url;
    private String roomId;
    private String createdAt;
}
