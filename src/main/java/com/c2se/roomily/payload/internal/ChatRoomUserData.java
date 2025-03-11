package com.c2se.roomily.payload.internal;

import com.c2se.roomily.enums.ChatRoomType;

import java.time.LocalDateTime;

public record ChatRoomUserData(
        String userId,
        String roomId,
        String roomName,
        ChatRoomType type,
        String lastMessage,
        LocalDateTime lastMessageTimeStamp,
        String lastMessageSender,
        Integer unreadCount,
        LocalDateTime lastReadMessageTimeStamp
) {
}
