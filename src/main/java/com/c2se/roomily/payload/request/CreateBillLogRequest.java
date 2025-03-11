package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CreateBillLogRequest {
    private String fromDate;
    private String toDate;
    private BigDecimal rentalCost;
    private String rentedRoomId;
}
