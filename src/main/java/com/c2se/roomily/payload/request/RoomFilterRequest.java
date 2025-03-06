package com.c2se.roomily.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomFilterRequest {
    private String city;
    private String district;
    private String ward;
    private String type;
    private Double minPrice;
    private Double maxPrice;
    private Integer minPeople;
    private Integer maxPeople;
    private String pivotId;
    private Integer limit;
    private String timestamp;
    private List<String> tagIds;
}