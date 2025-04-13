package com.c2se.roomily.event.pojo;

import lombok.Builder;
import org.springframework.context.ApplicationEvent;

public class SendPaymentLinkEvent extends ApplicationEvent {
    private final String rentedRoomId;
    private final String chatRoomId;
    private final String requesterId;

    @Builder
    public SendPaymentLinkEvent(Object source, String rentedRoomId, String chatRoomId, String requesterId) {
        super(source);
        this.rentedRoomId = rentedRoomId;
        this.chatRoomId = chatRoomId;
        this.requesterId = requesterId;
    }

    public static SendPaymentLinkEventBuilder builder(Object source) {
        return new SendPaymentLinkEventBuilder().source(source);
    }

    public String getRentedRoomId() {
        return rentedRoomId;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public String getRequesterId() {
        return requesterId;
    }
}
