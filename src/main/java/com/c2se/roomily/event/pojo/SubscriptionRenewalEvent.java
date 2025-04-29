package com.c2se.roomily.event.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

public class SubscriptionRenewalEvent extends ApplicationEvent {
    private final String subscriptionId;
    private final LocalDateTime renewalTime;

    @Builder
    public SubscriptionRenewalEvent(Object source, String subscriptionId, LocalDateTime renewalTime) {
        super(source);
        this.subscriptionId = subscriptionId;
        this.renewalTime = renewalTime;
    }

    public static SubscriptionRenewalEventBuilder builder(Object source) {
        return new SubscriptionRenewalEventBuilder().source(source);
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public LocalDateTime getRenewalTime() {
        return renewalTime;
    }
}
