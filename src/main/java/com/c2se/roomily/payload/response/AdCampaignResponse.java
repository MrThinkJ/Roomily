package com.c2se.roomily.payload.response;

import com.c2se.roomily.enums.AdCampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdCampaignResponse {
    private String id;
    private String name;
    private String pricingModel;
    private AdCampaignStatus status;
    private BigDecimal budget;
    private BigDecimal spentAmount;
    private BigDecimal dailyBudget;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String userId;
    private List<PromotedRoomResponse> promotedRooms;
    private CampaignStatisticsResponse statistics;
} 