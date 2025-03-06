package com.c2se.roomily.payload.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomPricingHistoryDao {
    private BigDecimal price;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

}
