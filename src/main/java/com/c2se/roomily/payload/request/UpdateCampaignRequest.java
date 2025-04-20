package com.c2se.roomily.payload.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCampaignRequest {
    private String name;
    @NotNull(message = "Campaign type cannot be null")
    @NotBlank(message = "Pricing model cannot be blank")
    private String pricingModel;
    @DecimalMin(value = "0.0", inclusive = false, message = "CPM rate must be greater than 0")
    private BigDecimal cpmRate;
    @DecimalMin(value = "0.0", inclusive = false, message = "Budget must be greater than 0")
    private BigDecimal budget;
    @DecimalMin(value = "0.0", inclusive = false, message = "Daily budget must be greater than 0")
    private BigDecimal dailyBudget;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;
} 