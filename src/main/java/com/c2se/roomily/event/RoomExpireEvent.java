package com.c2se.roomily.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomExpireEvent implements AppEvent {
    private String rentedRoomId;
    private String roomId;
    private String landlordId;
    private String userId;
}
