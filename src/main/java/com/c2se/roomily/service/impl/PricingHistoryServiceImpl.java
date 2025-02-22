package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.PricingHistory;
import com.c2se.roomily.repository.PricingHistoryRepository;
import com.c2se.roomily.repository.RoomRepository;
import com.c2se.roomily.service.PricingHistoryService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class PricingHistoryServiceImpl implements PricingHistoryService {
    PricingHistoryRepository pricingHistoryRepository;
    RoomRepository roomRepository;
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void collectDailyPricingData() {
    }

    @Override
    public List<PricingHistory> getPricingHistoryByRoomIdInRangeGroupBy(String roomId, String startDate, String endDate, String groupBy) {
        return null;
    }

    @Override
    public List<PricingHistory> getAveragePricingHistoryAroundByRoomIdInRangeGroupBy(String roomId, String squareMeter, String startDate, String endDate, String groupBy) {
        return null;
    }

    @Override
    public Double getAveragePriceByArea(String city, String district, String ward) {
        return null;
    }

    @Override
    public PricingHistory createPricingHistory(PricingHistory pricingHistory) {
        return null;
    }

    @Override
    public PricingHistory updatePricingHistory(PricingHistory pricingHistory) {
        return null;
    }

    @Override
    public void deletePricingHistory(Long id) {

    }
}
