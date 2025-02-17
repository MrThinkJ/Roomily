package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class CreateRoomRequest {
    private String title;
    private String description;
    private String address;
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
    private List<String> tagIds;
    private Double squareMeters;
}
