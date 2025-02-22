package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateRentedRoomRequest {
    private String startDate;
    private String endDate;
    private String roomId;
}
