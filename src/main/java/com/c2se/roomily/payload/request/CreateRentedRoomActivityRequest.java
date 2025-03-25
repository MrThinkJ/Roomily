package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateRentedRoomActivityRequest {
    private String rentedRoomId;
    private String message;
}
