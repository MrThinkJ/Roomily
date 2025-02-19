package com.c2se.roomily.payload.response;

import com.c2se.roomily.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {
    private String id;
    private String message;
    private String createdAt;
    private boolean isRead;
    private String imageUrl;
    private String roomId;
    private String senderId;
    private String recipientId;
}
