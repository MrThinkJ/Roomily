package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RentedRoomResponse {
    private String id;
    private String startDate;
    private String endDate;
    private Double duration;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String roomId;
    private String userId;
    private String landlordId;
}
