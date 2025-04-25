package com.c2se.roomily.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BillLogResponse {
    private String id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;
    private Double electricity;
    private Double water;
    private BigDecimal electricityBill;
    private BigDecimal waterBill;
    private String electricityImageUrl;
    private String waterImageUrl;
    private BigDecimal rentalCost;
    private String billStatus;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private String roomId;
    private String rentedRoomId;
    private boolean isRentalCostPaid;
}
