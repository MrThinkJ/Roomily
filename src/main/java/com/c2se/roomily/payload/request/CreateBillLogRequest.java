package com.c2se.roomily.payload.request;

import com.c2se.roomily.enums.BillStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CreateBillLogRequest {
    private String fromDate;
    private String toDate;
    private Double electricity;
    private Double water;
    private BigDecimal electricityBill;
    private BigDecimal waterBill;
    private BigDecimal rentalCost;
    private String rentedRoomId;
}
