package com.c2se.roomily.event.handler;

import com.c2se.roomily.event.pojo.AdCampaignStartEvent;
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
public class AdCampaignStartEventHandler {
    private final TaskScheduler taskScheduler;
    private final AdsService adsService;

    @EventListener
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleAdCampaignStartEvent(AdCampaignStartEvent event) {
        log.info("Ad campaign started: {}", event.getAdCampaignId());
        Runnable task = () -> {
            try {
                adsService.startCampaign(event.getAdCampaignId());
            } catch (Exception e) {
                log.error("Error starting ad campaign: {}", event.getAdCampaignId(), e);
            }
        };
        taskScheduler.schedule(task, event.getStartTime().atZone(ZoneId.of("Asia/Saigon")).toInstant());
        log.info("Scheduled task to start ad campaign: {}", event.getAdCampaignId());
    }
}
