package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateRentedRoomRequest {
    private String startDate;
    private String endDate;
    private Double duration;
    private String status;
}
