package com.c2se.roomily.event.pojo;

import lombok.Builder;
import org.springframework.context.ApplicationEvent;

public class RoomDeleteEvent extends ApplicationEvent {
    private final String roomId;

    @Builder
    public RoomDeleteEvent(Object source, String roomId) {
        super(source);
        this.roomId = roomId;
    }

    public static RoomDeleteEventBuilder builder(Object source) {
        return new RoomDeleteEventBuilder().source(source);
    }

    public String getRoomId() {
        return roomId;
    }
}
