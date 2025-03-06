package com.c2se.roomily.payload.response;

import com.c2se.roomily.entity.Tag;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class RoomResponse {
    private String id;
    private String title;
    private String description;
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
    private String deposit;
    private Set<Tag> tags;
    private Double squareMeters;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    private boolean isSubscribed;
}
