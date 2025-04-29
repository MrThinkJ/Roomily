package com.c2se.roomily.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
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
public class CreateCampaignRequest {
    @NotBlank(message = "Campaign name cannot be empty")
    private String name;
    @NotBlank(message = "Pricing model is required")
    private String pricingModel;
    @NotNull(message = "CPM rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "CPM rate must be greater than 0")
    private BigDecimal cpmRate;
    @NotNull(message = "Budget is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Budget must be greater than 0")
    private BigDecimal budget;
    @NotNull(message = "Daily budget is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Daily budget must be greater than 0")
    private BigDecimal dailyBudget;
    @NotNull(message = "Start date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;
} 