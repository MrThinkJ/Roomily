package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BillLogResponse {
    private String id;
    private String fromDate;
    private String toDate;
    private Double electricity;
    private Double water;
    private String electricityBill;
    private String waterBill;
    private String rentalCost;
    private String billStatus;
    private String createdAt;
    private String roomId;
    private String rentedRoomId;
}
