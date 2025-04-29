package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

import com.c2se.roomily.enums.FindPartnerPostType;

@Data
@Builder
public class CreateFindPartnerPostRequest {
    private String description;
    private Integer maxPeople;
    private String roomId;
    private List<String> currentParticipantPrivateIds;
    private FindPartnerPostType type;
    private String rentedRoomId;
}
