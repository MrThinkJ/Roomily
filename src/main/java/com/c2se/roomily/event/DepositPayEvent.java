package com.c2se.roomily.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepositPayEvent implements AppEvent{
    private String rentedRoomId;
}
