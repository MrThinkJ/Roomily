package com.c2se.roomily.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateRoomBoostRequest {
    
    @NotBlank(message = "Room ID is required")
    private String roomId;
    
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @NotNull(message = "Credits to use is required")
    @Min(value = 1, message = "Credits must be at least 1")
    private Integer creditsToUse;
    
    @NotNull(message = "Boost level is required")
    @Min(value = 1, message = "Boost level must be at least 1")
    private Integer boostLevel;
    private Double radiusKm;
}
