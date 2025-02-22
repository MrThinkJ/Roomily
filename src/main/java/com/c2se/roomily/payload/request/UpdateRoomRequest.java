package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UpdateRoomRequest {
    private String title;
    private String description;
    private String address;
    private String status;
    private String price;
    private Double latitude;
    private Double longitude;
    private String city;
    private String district;
    private String ward;
    private String electricPrice;
    private String waterPrice;
    private String type;
    private int maxPeople;
    private String deposit;
    private Set<String> tags;
    private Double squareMeters;
}
