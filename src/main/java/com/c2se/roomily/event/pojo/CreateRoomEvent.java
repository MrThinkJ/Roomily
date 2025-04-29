package com.c2se.roomily.event.pojo;

import lombok.Builder;
import org.springframework.context.ApplicationEvent;

public class CreateRoomEvent extends ApplicationEvent{
    private final String roomId;

    @Builder
    public CreateRoomEvent(Object source, String roomId) {
        super(source);
        this.roomId = roomId;
    }

    public static CreateRoomEventBuilder builder(Object source) {
        return new CreateRoomEventBuilder().source(source);
    }

    public String getRoomId() {
        return roomId;
    }
}
