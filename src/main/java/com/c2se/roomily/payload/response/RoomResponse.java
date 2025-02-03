package com.c2se.roomily.payload.response;

import com.c2se.roomily.entity.Tag;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
public class RoomResponse {
    private String id;
    private String name;
    private String address;
    private String status;
    private BigDecimal price;
    private Double latitude;
    private Double longitude;
    private String city;
    private String district;
    private String ward;
    private BigDecimal electricPrice;
    private BigDecimal waterPrice;
    private String type;
    private String nearbyAmenities;
    private Integer maxPeople;
    private String landlordId;
    private Set<Tag> tags;
}
