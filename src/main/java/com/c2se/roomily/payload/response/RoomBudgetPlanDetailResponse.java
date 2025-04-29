package com.c2se.roomily.payload.response;

import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomBudgetPlanDetailResponse {
    private RoomResponse roomResponse;
    private BigDecimal upFrontCost;
    private Integer estimatedMonthlyElectricityUsage;
    private Integer estimatedMonthlyWaterUsage;
    private boolean isIncludeWifi;
    private BigDecimal wifiCost;
    private boolean hasUserMonthlySalary;
    private BigDecimal monthlySalary;
    private BigDecimal maxBudget;
    private BigDecimal baseLineMinRentalCost;
    private BigDecimal baseLineMaxRentalCost;
    private BigDecimal baseLineMedianRentalCost;
    private BigDecimal averageElectricityCost;
    private BigDecimal averageWaterCost;
    private Set<Tag> matchedTags;
    private Set<Tag> unmatchedTags;
}
