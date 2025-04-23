package com.c2se.roomily.event.handler;

import com.c2se.roomily.event.pojo.AdCampaignExpireEvent;
import com.c2se.roomily.service.AdsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdsCampaignExpireEventHandler {
    private final TaskScheduler taskScheduler;
    private final AdsService adsService;

    @EventListener
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleAdCampaignExpireEvent(AdCampaignExpireEvent event) {
        Runnable task = () -> {
            try {
                adsService.expireCampaign(event.getCampaignId());
            } catch (Exception e) {
                log.error("Error expiring ad campaign: {}", event.getCampaignId(), e);
            }
        };
        taskScheduler.schedule(task, event.getExpirationTime().atZone(ZoneId.of("Asia/Saigon")).toInstant());
        log.info("Handling ad campaign expiration for campaign ID: {}", event.getCampaignId());
    }
}
