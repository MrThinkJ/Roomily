package com.c2se.roomily.payload.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddRoomRequest {
    @NotBlank(message = "Room ID cannot be empty")
    private String roomId;
    @NotNull(message = "CPC Bid amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "CPC Bid amount must be greater than 0")
    private BigDecimal cpcBid;
} 