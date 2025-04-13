package com.c2se.roomily.event.pojo;

import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.entity.RentedRoom;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

public class DepositPayEvent extends ApplicationEvent {
    private final RentedRoom rentedRoom;
    private final ChatRoom chatRoom;
    private final String requesterId;

    @Builder
    public DepositPayEvent(Object source, RentedRoom rentedRoom, ChatRoom chatRoom, String requesterId) {
        super(source);
        this.rentedRoom = rentedRoom;
        this.chatRoom = chatRoom;
        this.requesterId = requesterId;
    }

    public static DepositPayEventBuilder builder(Object source) {
        return new DepositPayEventBuilder().source(source);
    }

    public RentedRoom getRentedRoom() {
        return rentedRoom;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public String getRequesterId() {
        return requesterId;
    }
}
