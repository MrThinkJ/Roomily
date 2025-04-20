package com.c2se.roomily.event.handler;

import com.c2se.roomily.config.RabbitMQConfig;
import com.c2se.roomily.entity.AdCampaign;
import com.c2se.roomily.entity.CampaignStatistic;
import com.c2se.roomily.entity.PromotedRoom;
import com.c2se.roomily.enums.AdCampaignStatus;
import com.c2se.roomily.enums.PromotedRoomStatus;
import com.c2se.roomily.event.pojo.AdClickEvent;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.repository.AdCampaignRepository;
import com.c2se.roomily.repository.CampaignStatisticRepository;
import com.c2se.roomily.repository.PromotedRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdClickEventConsumer {
    private final AdCampaignRepository adCampaignRepository;
    private final PromotedRoomRepository promotedRoomRepository;
    private final CampaignStatisticRepository campaignStatisticRepository;

    @RabbitListener(queues = RabbitMQConfig.AD_CLICK_QUEUE)
    @Transactional
    public void processAdClickEvent(AdClickEvent event) {
        log.info("Processing ad click event: {}", event);
        try {
            PromotedRoom promotedRoom = promotedRoomRepository.findById(event.getPromotedRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Promoted Room", "Id", event.getPromotedRoomId()));
            AdCampaign campaign = promotedRoom.getAdCampaign();
            if (campaign.getStatus() != AdCampaignStatus.ACTIVE) {
                log.warn("Campaign {} is not active, ignoring click event", campaign.getId());
                return;
            }
            CampaignStatistic statistic = campaignStatisticRepository.findByAdCampaignId(campaign.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Campaign Statistic",
                                                                    "Campaign Id",
                                                                    campaign.getId()));
            statistic.setClicks(statistic.getClicks() + 1);
            log.info("Successfully processed click for campaign {}, room {}", 
                    campaign.getId(), promotedRoom.getRoom().getId());
        } catch (Exception e) {
            log.error("Error processing ad click event: {}", e.getMessage(), e);
        }
    }
} 