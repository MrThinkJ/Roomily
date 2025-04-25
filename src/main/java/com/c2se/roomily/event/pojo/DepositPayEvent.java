package com.c2se.roomily.event.pojo;

import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.entity.RentedRoom;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

public class DepositPayEvent extends ApplicationEvent {
    private final String rentedRoomId;
    private final ChatRoom chatRoom;
    private final String requesterId;

    @Builder
    public DepositPayEvent(Object source, String rentedRoomId, ChatRoom chatRoom, String requesterId) {
        super(source);
        this.rentedRoomId = rentedRoomId;
        this.chatRoom = chatRoom;
        this.requesterId = requesterId;
    }

    public static DepositPayEventBuilder builder(Object source) {
        return new DepositPayEventBuilder().source(source);
    }

    public String getRentedRoomId() {
        return rentedRoomId;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public String getRequesterId() {
        return requesterId;
    }
}
