package com.c2se.roomily.payload.internal;

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
public class FilterParameters {
    String city;
    String district;
    String ward;
    String roomType;
    BigDecimal minPrice;
    BigDecimal maxPrice;
    Integer minPeople;
    Integer maxPeople;
    boolean hasFindPartnerPost;
    String pivotId;
    int limit;
    LocalDateTime timestamp;
    List<String> tagIds;
}
