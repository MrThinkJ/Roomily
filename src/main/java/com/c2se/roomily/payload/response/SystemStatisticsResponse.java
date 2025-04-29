package com.c2se.roomily.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemStatisticsResponse {
    private long totalRentedRooms;
    private long totalActiveRentedRooms;
    private long totalUsers;
    private long totalActiveUsers;
    private long totalCompletedTransactions;
    private BigDecimal totalTransactionVolume;
} 