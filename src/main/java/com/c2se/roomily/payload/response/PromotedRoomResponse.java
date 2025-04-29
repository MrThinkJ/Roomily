package com.c2se.roomily.payload.response;

import com.c2se.roomily.enums.PromotedRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotedRoomResponse {
    private String id;
    private PromotedRoomStatus status;
    private BigDecimal bid;
    private String adCampaignId;
    private String roomId;
} 