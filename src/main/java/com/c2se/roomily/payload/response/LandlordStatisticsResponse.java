package com.c2se.roomily.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LandlordStatisticsResponse {
    private Double responseRate;
    private Long totalChatRooms;
    private Long respondedChatRooms;
    private Long averageResponseTimeMinutes;
    private Integer totalRentedRooms;
} 