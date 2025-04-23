package com.c2se.roomily.event.pojo;

import lombok.Builder;
import org.springframework.context.ApplicationEvent;

public class SendPaymentLinkEvent extends ApplicationEvent {
    private final String rentedRoomId;
    private final String chatRoomId;
    private final String requesterId;
    private final String chatMessageId;

    @Builder
    public SendPaymentLinkEvent(Object source,
                                String rentedRoomId,
                                String chatRoomId,
                                String requesterId,
                                String chatMessageId) {
        super(source);
        this.rentedRoomId = rentedRoomId;
        this.chatRoomId = chatRoomId;
        this.requesterId = requesterId;
        this.chatMessageId = chatMessageId;
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

    public String getChatMessageId() {
        return chatMessageId;
    }
}
