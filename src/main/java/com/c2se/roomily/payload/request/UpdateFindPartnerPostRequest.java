package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UpdateFindPartnerPostRequest {
    private String content;
    private String title;
    private Integer maxPeople;
}
