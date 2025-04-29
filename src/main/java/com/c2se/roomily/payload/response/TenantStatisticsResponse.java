package com.c2se.roomily.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantStatisticsResponse {
    private Double successRentedRate;
    private Double debtRate;
    private Integer totalRentedRooms;
    private Integer totalSuccessRented;
    private Integer totalLatePayments;
} 