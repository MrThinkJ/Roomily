package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateSubscriptionRequest {
    private String price;
    private String name;
    private String description;
    private Integer duration;
    private Integer includedCredits;
}
