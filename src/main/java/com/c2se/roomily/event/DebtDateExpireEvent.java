package com.c2se.roomily.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DebtDateExpireEvent implements AppEvent {
    private String rentedRoomId;
    private String userId;
    private String roomId;
    private String landlordId;
}
