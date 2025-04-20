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
public class CampaignStatisticsResponse {
    private Long totalImpressions;
    private Long totalClicks;
    private Long totalConversions;
    private BigDecimal totalSpent;
    private Double clickThroughRate; // CTR = clicks / impressions
    private Double conversionRate; // Conversion rate = conversions / clicks
    private Double costPerClick; // CPC = spent / clicks
    private Double costPerMille; // CPM = spent / (impressions / 1000)
} 