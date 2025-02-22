package com.c2se.roomily.service;

import com.c2se.roomily.entity.PricingHistory;

import java.util.List;

public interface PricingHistoryService {
    List<PricingHistory> getPricingHistoryByRoomIdInRangeGroupBy(String roomId,
                                                                 String startDate,
                                                                 String endDate,
                                                                 String groupBy);
    List<PricingHistory> getAveragePricingHistoryAroundByRoomIdInRangeGroupBy(String roomId,
                                                                              String squareMeter,
                                                                              String startDate,
                                                                              String endDate,
                                                                              String groupBy);
    Double getAveragePriceByArea(String city, String district, String ward);
//    Map<String, Double> getPriceTrends(String roomId, int lastNMonths);
//
//    List<PriceComparisonDTO> comparePricesWithSimilarRooms(String roomId, Double radiusKm);
//    List<PriceChangeDTO> getPriceChangesHistory(String roomId);
//    PriceStatisticsDTO getPriceStatisticsByType(String city,
//                                               String district,
//                                               RoomType type);
    PricingHistory createPricingHistory(PricingHistory pricingHistory);
    PricingHistory updatePricingHistory(PricingHistory pricingHistory);
    void deletePricingHistory(Long id);
}
