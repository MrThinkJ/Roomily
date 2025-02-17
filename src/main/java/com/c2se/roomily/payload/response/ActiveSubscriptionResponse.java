package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActiveSubscriptionResponse {
    private String id;
    private String name;
    private String description;
    private String details;
    private String price;
    private int duration;
    private boolean autoRenew;
    private String startDate;
    private String endDate;
    private String subscriptionId;
}
