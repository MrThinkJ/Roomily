package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchedRoomResponse {
    private String roomId;
    private String roomTitle;
    private String roomDescription;
    private String roomAddress;
    private Double squareMeters;
    private String imageUrl;
    private String roomType;
    private String city;
    private String district;
    private String ward;
    private Double latitude;
    private Double longitude;
    private Integer numberOfTagsMatched;
    private Integer numberOfTags;
    private Double tagSimilarity;
}
