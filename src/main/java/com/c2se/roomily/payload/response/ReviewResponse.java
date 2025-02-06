package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private String id;
    private String content;
    private int rating;
    private String roomId;
    private String userId;
    private String userName;
    private String userAvatar;
    private String createdAt;
    private String updatedAt;
}
