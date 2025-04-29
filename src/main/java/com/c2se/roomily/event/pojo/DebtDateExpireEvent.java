package com.c2se.roomily.event.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

public class DebtDateExpireEvent extends ApplicationEvent {
    private final String rentedRoomId;
    private final String userId;
    private final String roomId;
    private final String landlordId;

    @Builder
    public DebtDateExpireEvent(Object source, String rentedRoomId, String userId, String roomId, String landlordId) {
        super(source);
        this.rentedRoomId = rentedRoomId;
        this.userId = userId;
        this.roomId = roomId;
        this.landlordId = landlordId;
    }

    public static DebtDateExpireEventBuilder builder(Object source) {
        return new DebtDateExpireEventBuilder().source(source);
    }

    public String getRentedRoomId() {
        return rentedRoomId;
    }

    public String getUserId() {
        return userId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getLandlordId() {
        return landlordId;
    }
}
