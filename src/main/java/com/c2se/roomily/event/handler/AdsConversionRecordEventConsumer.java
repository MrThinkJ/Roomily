package com.c2se.roomily.event.handler;

import com.c2se.roomily.config.RabbitMQConfig;
import com.c2se.roomily.entity.AdClickLog;
import com.c2se.roomily.entity.CampaignStatistic;
import com.c2se.roomily.event.pojo.AdConversionRecordEvent;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.repository.AdsClickLogRepository;
import com.c2se.roomily.repository.CampaignStatisticRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdsConversionRecordEventConsumer {
    private final CampaignStatisticRepository campaignStatisticRepository;
    private final AdsClickLogRepository adsClickLogRepository;
    @RabbitListener(queues = RabbitMQConfig.ADS_CONVERSION_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void processAdConversionRecordEvent(AdConversionRecordEvent adConversionRecordEvent){
        AdClickLog adClickLog = adsClickLogRepository.findById(adConversionRecordEvent.getAdClickId()).orElseThrow(
                () -> new RuntimeException("Ad click log not found")
        );
        CampaignStatistic campaignStatistic = campaignStatisticRepository.findByAdCampaignId(
                adClickLog.getCampaignId()).orElseThrow(
                () -> new ResourceNotFoundException("Campaign Statistic", "campaignId", adClickLog.getCampaignId())
        );
        campaignStatistic.setConversionCount(campaignStatistic.getConversionCount() + 1);
        campaignStatisticRepository.save(campaignStatistic);
    }
}
