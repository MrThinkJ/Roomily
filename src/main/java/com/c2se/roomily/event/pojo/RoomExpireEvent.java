package com.c2se.roomily.event.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

public class RoomExpireEvent extends ApplicationEvent {
    private final String rentedRoomId;
    private final String roomId;
    private final String landlordId;
    private final String userId;

    @Builder
    private RoomExpireEvent(Object source, String rentedRoomId, String roomId, String landlordId, String userId) {
        super(source);
        this.rentedRoomId = rentedRoomId;
        this.roomId = roomId;
        this.landlordId = landlordId;
        this.userId = userId;
    }

    public static RoomExpireEventBuilder builder(Object source) {
        return new RoomExpireEventBuilder().source(source);
    }

    public String getRentedRoomId() {
        return rentedRoomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getLandlordId() {
        return landlordId;
    }

    public String getUserId() {
        return userId;
    }
}
