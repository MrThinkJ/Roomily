package com.c2se.roomily.event.pojo;

import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.entity.RentedRoom;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepositPayEvent implements AppEvent{
    private RentedRoom rentedRoom;
    private ChatRoom chatRoom;
    private String requesterId;
}
