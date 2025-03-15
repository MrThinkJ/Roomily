package com.c2se.roomily.payload.internal;

import com.c2se.roomily.enums.ChatRoomType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatRoomUserData{
    private String userId;
    private String chatRoomId;
    private String roomName;
    private ChatRoomType type;
    private String lastMessage;
    private LocalDateTime lastMessageTimeStamp;
    private String lastMessageSender;
    private Integer unreadCount;
    private LocalDateTime lastReadMessageTimeStamp;
}
