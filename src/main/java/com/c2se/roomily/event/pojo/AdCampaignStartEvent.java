package com.c2se.roomily.event.pojo;

import lombok.Builder;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

public class AdCampaignStartEvent extends ApplicationEvent {
    private final String adCampaignId;
    private final LocalDateTime startTime;

    @Builder
    public AdCampaignStartEvent(Object source, String adCampaignId, LocalDateTime startTime) {
        super(source);
        this.adCampaignId = adCampaignId;
        this.startTime = startTime;
    }

    public static AdCampaignStartEventBuilder builder(Object source) {
        return new AdCampaignStartEventBuilder().source(source);
    }

    public String getAdCampaignId() {
        return adCampaignId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }
}
