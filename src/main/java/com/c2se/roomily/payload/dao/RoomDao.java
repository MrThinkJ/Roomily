package com.c2se.roomily.payload.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomDao {
    private String id;
    private String address;
    private String city;
    private String description;
    private String district;
    private BigDecimal electricPrice;
    private Integer maxPeople;
    private String nearbyAmenities;
    private BigDecimal price;
    private Double squareMeters;
    private String title;
    private String type;
    private String ward;
    private BigDecimal waterPrice;
    private Timestamp createdAt;
    private String deposit;
    private String status;
    private Timestamp updatedAt;
    private Double latitude;
    private Double longitude;
    private String landlordId;
}
