package com.c2se.roomily.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
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
    @JsonProperty("isSubscribed")
    private boolean isSubscribed;
    private List<String> tagIds;
}