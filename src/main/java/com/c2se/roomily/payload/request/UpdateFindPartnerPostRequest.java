package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateFindPartnerPostRequest {
    private String description;
    private Integer maxPeople;
}
