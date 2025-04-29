package com.c2se.roomily.event.pojo;

import lombok.Builder;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

public class AdCampaignExpireEvent extends ApplicationEvent {
    private final String campaignId;
    private final LocalDateTime expirationTime;

    @Builder
    public AdCampaignExpireEvent(Object source, String campaignId, LocalDateTime expirationTime) {
        super(source);
        this.campaignId = campaignId;
        this.expirationTime = expirationTime;
    }

    public static AdCampaignExpireEventBuilder builder(Object source) {
        return new AdCampaignExpireEventBuilder().source(source);
    }

    public String getCampaignId() {
        return campaignId;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }
}
