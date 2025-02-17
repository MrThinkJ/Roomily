package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSubscriptionResponse {
    private String id;
    private String startDate;
    private String endDate;
    private String userId;
    private String subscriptionId;
    private String subscriptionName;
}
