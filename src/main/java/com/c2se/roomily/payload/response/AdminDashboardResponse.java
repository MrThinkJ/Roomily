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
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalLandlords;
    private long totalTenants;
    private long activeUsers;
    private long newUsersThisMonth;
    private BigDecimal totalSystemBalance;
    private BigDecimal totalWithdrawalsThisMonth;
    private BigDecimal totalDepositsThisMonth;
    private long pendingWithdrawalsCount;
} 