package com.c2se.roomily.service;

import com.c2se.roomily.payload.response.LandlordStatisticsResponse;
import com.c2se.roomily.payload.response.TenantStatisticsResponse;

import java.math.BigDecimal;

public interface StatisticsService {
    Double getTenantSuccessRentedRate(String userId);
    
    Double getTenantDebtRate(String userId);
    
    Double getLandlordResponseRate(String landlordId);
    LandlordStatisticsResponse getLandlordStatistics(String landlordId);
    TenantStatisticsResponse getTenantStatistics(String tenantId);
    
    // New methods for system statistics
    long getTotalRentedRooms();
    
    long getTotalActiveRentedRooms();
} 