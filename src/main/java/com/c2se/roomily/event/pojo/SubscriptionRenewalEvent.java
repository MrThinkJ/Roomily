package com.c2se.roomily.event.pojo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SubscriptionRenewalEvent implements AppEvent {
    private String subscriptionId;
    private LocalDateTime renewalTime;
}
