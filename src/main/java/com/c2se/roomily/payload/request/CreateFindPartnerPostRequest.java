package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateFindPartnerPostRequest {
    private Integer maxPeople;
    private String roomId;
    private List<String> currentParticipantPrivateIds;
}
