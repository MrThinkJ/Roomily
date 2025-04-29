package com.c2se.roomily.service;

import com.c2se.roomily.entity.AdClickLog;
import com.c2se.roomily.payload.request.*;
import com.c2se.roomily.payload.response.AdCampaignResponse;
import com.c2se.roomily.payload.response.AdClickResponse;
import com.c2se.roomily.payload.response.PromotedRoomResponse;

import java.math.BigDecimal;
import java.util.List;

public interface AdsService {
    AdClickLog getAdClickLogById(String adClickId);
    void createCampaign(String userId, CreateCampaignRequest request);

    void updateCampaign(String userId, String campaignId, UpdateCampaignRequest request);

    void pauseCampaign(String userId, String campaignId);

    void resumeCampaign(String userId, String campaignId);

    void deleteCampaign(String userId, String campaignId);

    AdCampaignResponse getCampaign(String userId, String campaignId);

    List<AdCampaignResponse> getUserCampaigns(String userId);

    void addRoomToCampaign(String userId, String campaignId, AddRoomRequest request);

    void updatePromotedRoom(String userId, String promotedRoomId, UpdatePromotedRoomRequest request);

    void removeRoomFromCampaign(String userId, String promotedRoomId);

    List<PromotedRoomResponse> getPromotedRoomsByCampaign(String userId, String campaignId);

    AdClickResponse recordClick(AdClickRequest adClickRequest);

    void recordImpression(AdImpressionRequest adImpressionRequest);

    void processDailyCampaigns();

    void startCampaign(String campaignId);

    void expireCampaign(String campaignId);

    void chargeForUserClick(String promotedRoomId, BigDecimal cost);
} 