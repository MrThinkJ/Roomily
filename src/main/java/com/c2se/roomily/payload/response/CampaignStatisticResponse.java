package com.c2se.roomily.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignStatisticResponse {
    private String id;
    private LocalDate date;
    private Long impressions;
    private Long clicks;
    private Long conversions;
    private BigDecimal dailySpend;
    private String adCampaignId;
} 