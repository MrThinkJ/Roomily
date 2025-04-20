package com.c2se.roomily.payload.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePromotedRoomRequest {
    @DecimalMin(value = "0.0", inclusive = false, message = "CPC bid amount must be greater than 0")
    private BigDecimal cpcBid;
} 