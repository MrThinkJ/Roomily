package com.c2se.roomily.service.impl;

import com.c2se.roomily.config.RabbitMQConfig;
import com.c2se.roomily.entity.*;
import com.c2se.roomily.enums.*;
import com.c2se.roomily.event.pojo.AdCampaignExpireEvent;
import com.c2se.roomily.event.pojo.AdCampaignStartEvent;
import com.c2se.roomily.event.pojo.AdClickEvent;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.*;
import com.c2se.roomily.payload.response.*;
import com.c2se.roomily.repository.*;
import com.c2se.roomily.service.AdsService;
import com.c2se.roomily.service.EventService;
import com.c2se.roomily.service.RoomService;
import com.c2se.roomily.service.UserService;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdsServiceImpl implements AdsService {
    // TODO: Implement redis count instead of store impression log in DB
    // TODO: Implement batch-processing for counting click and impression
    private final AdsCampaignRepository adsCampaignRepository;
    private final PromotedRoomRepository promotedRoomRepository;
    private final CampaignStatisticRepository campaignStatisticRepository;
    private final UserService userService;
    private final RoomService roomService;
    private final EventService eventService;
    private final AdsClickDeDupRepository adsClickDeDupRepository;
    private final AdsClickLogRepository adsClickLogRepository;
    private final AdsImpressionLogRepository adsImpressionLogRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public AdClickLog getAdClickLogById(String adClickId) {
        return adsClickLogRepository.findById(adClickId)
                .orElseThrow(() -> new ResourceNotFoundException("Ad Click Log", "id", adClickId));
    }

    @Override
    @Transactional
    public void createCampaign(String userId, CreateCampaignRequest request) {
        User user = userService.getUserEntity(userId);

        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "End date must be after start date");
        }

        if (request.getEndDate() != null && request.getEndDate().isBefore(LocalDateTime.now())) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "End date must be in the future");
        }

        if (request.getBudget().compareTo(BigDecimal.ZERO) <= 0) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "Budget must be greater than zero");
        }

        if (request.getBudget().compareTo(user.getBalance()) > 0){
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "Insufficient balance");
        }
        
        if (request.getDailyBudget().compareTo(BigDecimal.ZERO) <= 0) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "Daily budget must be greater than zero");
        }
        
        if (request.getDailyBudget().compareTo(request.getBudget()) > 0) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "Daily budget cannot exceed total budget");
        }

        if (request.getPricingModel().equals(PricingModel.CPM.name()) && request.getCpmRate() == null){
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "CPM rate must be provided for CPM pricing model");
        }
        
        LocalDateTime now = LocalDateTime.now();

        AdCampaignStatus initialStatus = request.getStartDate().isBefore(now) || request.getStartDate().isEqual(now) 
                ? AdCampaignStatus.ACTIVE
                : AdCampaignStatus.DRAFT;

        AdCampaign campaign = AdCampaign.builder()
                .name(request.getName())
                .pricingModel(PricingModel.valueOf(request.getPricingModel()))
                .cpmRate(request.getCpmRate())
                .budget(request.getBudget())
                .spentAmount(BigDecimal.ZERO)
                .dailyBudget(request.getDailyBudget())
                .dailySpentAmount(BigDecimal.ZERO)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(initialStatus)
                .user(user)
                .promotedRooms(new ArrayList<>())
                .campaignStatistics(new ArrayList<>())
                .build();
        adsCampaignRepository.save(campaign);

        CampaignStatistic campaignStatistic = CampaignStatistic.builder()
                .adCampaign(campaign)
                .clicks(0L)
                .conversionCount(0L)
                .impressionCount(0L)
                .build();
        campaignStatisticRepository.save(campaignStatistic);

        String startDay = request.getStartDate().getDayOfMonth()+"/"+
                request.getStartDate().getMonthValue()+"/"+
                request.getStartDate().getYear();
        String endDay = request.getEndDate().getDayOfMonth()+"/"+
                request.getEndDate().getMonthValue()+"/"+
                request.getEndDate().getYear();
        String currentDay = LocalDateTime.now().getDayOfMonth()+"/"+
                LocalDateTime.now().getMonthValue()+"/"+
                LocalDateTime.now().getYear();
        if (campaign.getStatus() == AdCampaignStatus.DRAFT
                && startDay.equalsIgnoreCase(currentDay)) {
            eventService.publishEvent(
                    AdCampaignStartEvent.builder(this)
                            .adCampaignId(campaign.getId())
                            .startTime(campaign.getStartDate())
                            .build()
            );
            return;
        }
        if (campaign.getStatus() == AdCampaignStatus.ACTIVE &&
                endDay.equalsIgnoreCase(currentDay)) {
            eventService.publishEvent(
                    AdCampaignExpireEvent.builder(this)
                            .campaignId(campaign.getId())
                            .expirationTime(campaign.getEndDate())
                            .build()
            );
        }
    }

    @Override
    @Transactional
    public void updateCampaign(String userId, String campaignId, UpdateCampaignRequest request) {
        AdCampaign campaign = getCampaignAndValidateOwnership(userId, campaignId);

        if (request.getName() != null) {
            campaign.setName(request.getName());
        }
        
        if (request.getBudget() != null) {
            if (request.getBudget().compareTo(campaign.getSpentAmount()) < 0) {
                throw new APIException(HttpStatus.BAD_REQUEST,
                                       ErrorCode.FLEXIBLE_ERROR,
                                       "New budget cannot be less than amount already spent");
            }
            campaign.setBudget(request.getBudget());
        }
        
        if (request.getDailyBudget() != null) {
            if (request.getDailyBudget().compareTo(BigDecimal.ZERO) <= 0) {
                throw new APIException(HttpStatus.BAD_REQUEST,
                                       ErrorCode.FLEXIBLE_ERROR,
                                       "Daily budget must be greater than zero");
            }
            campaign.setDailyBudget(request.getDailyBudget());
        }

        if (request.getPricingModel().equals(PricingModel.CPM.name()) && request.getCpmRate() == null) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "CPM rate must be provided for CPM pricing model");
        }

        if(request.getPricingModel().equals(PricingModel.CPC.name()) && request.getCpmRate() != null) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "CPM rate cannot be provided for CPC pricing model");
        }

        if (request.getCpmRate() != null) {
            campaign.setCpmRate(request.getCpmRate());
        }
        
        if (request.getStartDate() != null) {
            campaign.setStartDate(request.getStartDate());
        }
        
        if (request.getEndDate() != null) {
            campaign.setEndDate(request.getEndDate());
        }
        
        adsCampaignRepository.save(campaign);
    }

    @Override
    @Transactional
    public void pauseCampaign(String userId, String campaignId) {
        AdCampaign campaign = getCampaignAndValidateOwnership(userId, campaignId);
        if (campaign.getStatus() != AdCampaignStatus.ACTIVE) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "Only active campaigns can be paused");
        }
        campaign.setStatus(AdCampaignStatus.PAUSED);
        AdCampaign updatedCampaign = adsCampaignRepository.save(campaign);
        promotedRoomRepository.updateStatusByCampaignId(PromotedRoomStatus.PAUSED, updatedCampaign.getId());
    }

    @Override
    @Transactional
    public void resumeCampaign(String userId, String campaignId) {
        AdCampaign campaign = getCampaignAndValidateOwnership(userId, campaignId);
        
        if (campaign.getStatus() != AdCampaignStatus.PAUSED) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "Only paused campaigns can be resumed");
        }

        if (campaign.getSpentAmount().compareTo(campaign.getBudget()) >= 0) {
            campaign.setStatus(AdCampaignStatus.OUT_OF_BUDGET);
        }
        else if (campaign.getEndDate() != null && campaign.getEndDate().isBefore(LocalDateTime.now())) {
            campaign.setStatus(AdCampaignStatus.COMPLETED);
        }
        else if (campaign.getStartDate().isAfter(LocalDateTime.now())) {
            campaign.setStatus(AdCampaignStatus.DRAFT);
        }
        else {
            campaign.setStatus(AdCampaignStatus.ACTIVE);
        }
        
        adsCampaignRepository.save(campaign);
        if (campaign.getStatus() == AdCampaignStatus.ACTIVE) {
            promotedRoomRepository.updateStatusByCampaignId(PromotedRoomStatus.ACTIVE, campaignId);
        }
    }

    @Override
    public void deleteCampaign(String userId, String campaignId) {
        AdCampaign campaign = getCampaignAndValidateOwnership(userId, campaignId);
        if (campaign.getStatus() == AdCampaignStatus.ACTIVE) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "Active campaigns cannot be deleted");
        }
        adsCampaignRepository.delete(campaign);
    }

    @Override
    public AdCampaignResponse getCampaign(String userId, String campaignId) {
        AdCampaign campaign = getCampaignAndValidateOwnership(userId, campaignId);
        return mapToAdCampaignResponse(campaign);
    }

    @Override
    public List<AdCampaignResponse> getUserCampaigns(String userId) {
        List<AdCampaign> campaigns = adsCampaignRepository.findByUserId(userId);
        return campaigns.stream()
                .map(this::mapToAdCampaignResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addRoomToCampaign(String userId, String campaignId, AddRoomRequest request) {
        AdCampaign campaign = getCampaignAndValidateOwnership(userId, campaignId);

        Room room = roomService.getRoomEntityById(request.getRoomId());
        if (!room.getLandlord().getId().equals(userId)) {
            throw new APIException(HttpStatus.FORBIDDEN,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "You don't own this room");
        }

        if (campaign.getPricingModel() == PricingModel.CPC
                && request.getCpcBid().compareTo(BigDecimal.ZERO) <= 0) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "CPC bid must be greater than zero for CPC pricing model");
        }

        if (campaign.getPricingModel() == PricingModel.CPM) {
            request.setCpcBid(BigDecimal.ZERO);
        }

        boolean roomAlreadyInCampaign = campaign.getPromotedRooms().stream()
                .anyMatch(pr -> pr.getRoom().getId().equals(request.getRoomId()));

        if (roomAlreadyInCampaign) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "This room is already in the campaign");
        }

        PromotedRoomStatus status = campaign.getStatus() == AdCampaignStatus.ACTIVE 
                ? PromotedRoomStatus.ACTIVE 
                : PromotedRoomStatus.PAUSED;

        PromotedRoom promotedRoom = PromotedRoom.builder()
                .room(room)
                .adCampaign(campaign)
                .cpcBid(request.getCpcBid())
                .status(status)
                .build();

        promotedRoomRepository.save(promotedRoom);
    }

    @Override
    @Transactional
    public void updatePromotedRoom(String userId, String promotedRoomId, UpdatePromotedRoomRequest request) {
        PromotedRoom promotedRoom = getPromotedRoomAndValidateOwnership(userId, promotedRoomId);
        AdCampaign campaign = promotedRoom.getAdCampaign();
        if (campaign.getPricingModel() == PricingModel.CPC
                && request.getCpcBid() == null) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "CPC bid must be provided for CPC pricing model");
        }

        if (campaign.getPricingModel() == PricingModel.CPM
                && request.getCpcBid() != null) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "CPC bid cannot be provided for CPM pricing model");
        }
        if (request.getCpcBid() != null) {
            promotedRoom.setCpcBid(request.getCpcBid());
        }
        promotedRoomRepository.save(promotedRoom);
    }

    @Override
    @Transactional
    public void removeRoomFromCampaign(String userId, String promotedRoomId) {
        PromotedRoom promotedRoom = getPromotedRoomAndValidateOwnership(userId, promotedRoomId);
        promotedRoomRepository.delete(promotedRoom);
    }

    @Override
    public List<PromotedRoomResponse> getPromotedRoomsByCampaign(String userId, String campaignId) {
        AdCampaign campaign = getCampaignAndValidateOwnership(userId, campaignId);
        
        List<PromotedRoom> promotedRooms = promotedRoomRepository.findByAdCampaignId(campaign.getId());
        
        return promotedRooms.stream()
                .map(this::mapToPromotedRoomResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AdClickResponse recordClick(AdClickRequest adClickRequest) {
        if (!adsClickDeDupRepository.save(adClickRequest)) {
            return AdClickResponse.builder().adClickId(null).status("duplicate").build();
        }

        PromotedRoom promotedRoom = promotedRoomRepository.findById(adClickRequest.getPromotedRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Promoted Room", "id", adClickRequest.getPromotedRoomId()));
        Room room = promotedRoom.getRoom();
        AdCampaign campaign = promotedRoom.getAdCampaign();

        if (room.getStatus() != RoomStatus.AVAILABLE) {
            return AdClickResponse.builder().adClickId(null).status("error").build();
        }

        if (campaign.getStatus() != AdCampaignStatus.ACTIVE || promotedRoom.getStatus() != PromotedRoomStatus.ACTIVE) {
            return AdClickResponse.builder().adClickId(null).status("inactive").build();
        }

        BigDecimal cost = promotedRoom.getCpcBid();
        chargeForUserClick(promotedRoom.getId(), cost);
        AdClickLog adClickLog = AdClickLog.builder()
                .campaignId(campaign.getId())
                .promotedRoomId(promotedRoom.getId())
                .roomId(promotedRoom.getRoom().getId())
                .userId(adClickRequest.getUserId())
                .ipAddress(adClickRequest.getIpAddress())
                .timestamp(LocalDateTime.now())
                .cost(promotedRoom.getCpcBid())
                .build();
        AdClickLog savedAdClickLog = adsClickLogRepository.save(adClickLog);
        rabbitTemplate.convertAndSend(RabbitMQConfig.ADS_EXCHANGE_NAME,
                                      RabbitMQConfig.ADS_CLICK_ROUTING_KEY,
                                      AdClickEvent.builder()
                                              .promotedRoomId(promotedRoom.getId())
                                              .timestamp(savedAdClickLog.getTimestamp())
                                              .build());
        return AdClickResponse.builder().adClickId(savedAdClickLog.getId()).status("success").build();
    }

    @Override
    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public void chargeForUserClick(String promotedRoomId, BigDecimal cost) {
        PromotedRoom promotedRoom = promotedRoomRepository.findById(promotedRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Promoted Room", "id", promotedRoomId));
        AdCampaign campaign = promotedRoom.getAdCampaign();
        User user = campaign.getUser();
        if (campaign.getStatus() != AdCampaignStatus.ACTIVE || promotedRoom.getStatus() != PromotedRoomStatus.ACTIVE) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "Promoted room is not active");
        }
        if (campaign.getSpentAmount().add(cost).compareTo(campaign.getBudget()) > 0) {
            campaign.setStatus(AdCampaignStatus.OUT_OF_BUDGET);
            adsCampaignRepository.save(campaign);
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "Campaign budget exceeded");
        }
        if (user.getBalance().compareTo(cost) < 0) {
            campaign.setStatus(AdCampaignStatus.INSUFFICIENT_FUNDS);
            adsCampaignRepository.save(campaign);
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "Insufficient balance");
        }
        campaign.setSpentAmount(campaign.getSpentAmount().add(cost));
        campaign.setDailySpentAmount(campaign.getDailySpentAmount().add(cost));
        user.setBalance(user.getBalance().subtract(cost));
        adsCampaignRepository.save(campaign);
        userService.saveUser(user);
    }

    @Override
    public void recordImpression(AdImpressionRequest adImpressionRequest) {
        for (String promotedRoomId : adImpressionRequest.getPromotedRoomIds()){
            PromotedRoom promotedRoom = promotedRoomRepository.findById(promotedRoomId)
                    .orElseThrow(() -> new ResourceNotFoundException("Promoted Room", "id", promotedRoomId));
            AdCampaign campaign = promotedRoom.getAdCampaign();
            if (campaign.getStatus() != AdCampaignStatus.ACTIVE || promotedRoom.getStatus() != PromotedRoomStatus.ACTIVE) {
                continue;
            }
            AdImpressionLog adImpressionLog = AdImpressionLog.builder()
                    .campaignId(campaign.getId())
                    .promotedRoomId(promotedRoom.getId())
                    .roomId(promotedRoom.getRoom().getId())
                    .isProcessed(false)
                    .userId(adImpressionRequest.getUserId())
                    .timestamp(LocalDateTime.now())
                    .build();
            adsImpressionLogRepository.save(adImpressionLog);
        }
    }

//    @Scheduled(fixedRate = 600000)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processImpressions(){
        List<AdImpressionLog> unprocessedImpressions = adsImpressionLogRepository.findByIsProcessedFalse();
        if (unprocessedImpressions.isEmpty()) return;
        Map<String, List<String>> groupByCampaignId = unprocessedImpressions.stream()
                .collect(Collectors.groupingByConcurrent(AdImpressionLog::getCampaignId,
                        Collectors.mapping(AdImpressionLog::getId, Collectors.toList())));
        Map<String, Integer> countByPromotedRoomId = unprocessedImpressions.stream()
                .collect(Collectors.groupingByConcurrent(AdImpressionLog::getPromotedRoomId,
                        Collectors.summingInt(e -> 1)));
        for (String campaignId : groupByCampaignId.keySet()){
            AdCampaign campaign;
            try{
                campaign = adsCampaignRepository.findById(campaignId)
                        .orElseThrow(() -> new ResourceNotFoundException("Ad Campaign", "id", campaignId));
            } catch (ResourceNotFoundException e){
                adsImpressionLogRepository.markAsProcessed(groupByCampaignId.get(campaignId));
                continue;
            }
            User landlord = campaign.getUser();
            long impressionCount = groupByCampaignId.get(campaignId).size();
            BigDecimal cpmRate = campaign.getCpmRate();
            BigDecimal cost = BigDecimal.valueOf(impressionCount).divide(BigDecimal.valueOf(1000), MathContext.DECIMAL64)
                    .multiply(cpmRate);
            if (campaign.getSpentAmount().add(cost).compareTo(campaign.getBudget()) > 0) {
                campaign.setStatus(AdCampaignStatus.OUT_OF_BUDGET);
                adsCampaignRepository.save(campaign);
                continue;
            }
            if (campaign.getDailySpentAmount().add(cost).compareTo(campaign.getDailyBudget()) > 0) {
                campaign.setStatus(AdCampaignStatus.OUT_OF_DAILY_BUDGET);
                adsCampaignRepository.save(campaign);
                continue;
            }
            if (cost.compareTo(landlord.getBalance()) > 0){
                campaign.setStatus(AdCampaignStatus.INSUFFICIENT_FUNDS);
                adsCampaignRepository.save(campaign);
                continue;
            }
            landlord.setBalance(landlord.getBalance().subtract(cost));
            campaign.setSpentAmount(campaign.getSpentAmount().add(cost));
            campaign.setDailySpentAmount(campaign.getDailySpentAmount().add(cost));
            adsCampaignRepository.save(campaign);
            userService.saveUser(landlord);
            CampaignStatistic campaignStatistic = campaignStatisticRepository.findByAdCampaignId(campaignId)
                    .orElseThrow(() -> new ResourceNotFoundException("Campaign Statistic", "id", campaignId));
            campaignStatistic.setImpressionCount(campaignStatistic.getImpressionCount() + impressionCount);
            campaignStatisticRepository.save(campaignStatistic);
            adsImpressionLogRepository.markAsProcessed(groupByCampaignId.get(campaignId));
        }
        for (String promotedRoomId : countByPromotedRoomId.keySet()){
            PromotedRoom promotedRoom;
            try{
                promotedRoom = promotedRoomRepository.findById(promotedRoomId)
                        .orElseThrow(() -> new ResourceNotFoundException("Promoted Room", "id", promotedRoomId));
            } catch (ResourceNotFoundException e){
                adsImpressionLogRepository.markAsProcessed(groupByCampaignId.get(promotedRoomId));
                continue;
            }
            promotedRoom.setImpressionCount(promotedRoom.getImpressionCount() +
                                                    countByPromotedRoomId.get(promotedRoomId));
            promotedRoomRepository.save(promotedRoom);
        }
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional(readOnly = true)
    public void processDailyCampaigns() {
        log.info("Processing daily campaigns");
        List<String> outOfBudgetCampaignIds = adsCampaignRepository.findActiveCampaignDailySpentGreaterThanZero();
        adsCampaignRepository.resetDailySpentAmount(outOfBudgetCampaignIds);
        log.info("Processing start campaigns");
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        List<AdCampaign> nonStartedCampaigns = adsCampaignRepository.findByStartDateBetween(startOfDay, endOfDay);
        for (AdCampaign campaign : nonStartedCampaigns) {
            if (campaign.getStatus() == AdCampaignStatus.DRAFT) {
                eventService.publishEvent(
                        AdCampaignStartEvent.builder(this)
                                .adCampaignId(campaign.getId())
                                .startTime(campaign.getStartDate() != null ?
                                                        campaign.getStartDate() : LocalDateTime.now())
                                .build()
                );
            }
        }
        List<AdCampaign> expiredCampaigns = adsCampaignRepository.findByEndDateBetween(startOfDay, endOfDay);
        for (AdCampaign campaign : expiredCampaigns) {
            if (campaign.getStatus() == AdCampaignStatus.ACTIVE) {
                eventService.publishEvent(
                        AdCampaignExpireEvent.builder(this)
                                .campaignId(campaign.getId())
                                .expirationTime(campaign.getEndDate() != null ?
                                                        campaign.getEndDate() : LocalDateTime.now())
                                .build()
                );
            }
        }
        List<AdCampaign> outOfDailyBudgetCampaigns = adsCampaignRepository.findByStatusAndEndDateAfter(
                AdCampaignStatus.OUT_OF_DAILY_BUDGET, LocalDateTime.now());
        for (AdCampaign campaign : outOfDailyBudgetCampaigns) {
                reBudgetCampaign(campaign);
        }
    }

    @Override
    public void startCampaign(String campaignId) {
        AdCampaign campaign = adsCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", campaignId));
//        List<PromotedRoom> activePromotedRooms = promotedRoomRepository.findActiveByUserId(
//                campaign.getUser().getId());
//        Map<String, PromotedRoom> activePromotedRoomMap = activePromotedRooms.stream()
//                .collect(Collectors.toMap(pr -> pr.getRoom().getId(), pr -> pr));
        campaign.setStatus(AdCampaignStatus.ACTIVE);
        adsCampaignRepository.save(campaign);
        List<PromotedRoom> campaignPromotedRooms = campaign.getPromotedRooms();
        promotedRoomRepository.updateStatusByCampaignId(PromotedRoomStatus.ACTIVE, campaignId);
//        List<String> neededPromotedRoomIds = new ArrayList<>();
//        List<String> neededDeactivatedRoomIds = new ArrayList<>();
//
//        for (PromotedRoom campaignPromotedRoom : campaignPromotedRooms) {
//            if (activePromotedRoomMap.containsKey(campaignPromotedRoom.getRoom().getId())) {
//                PromotedRoom activePromotedRoom = activePromotedRoomMap.get(campaignPromotedRoom.getRoom().getId());
//                if (campaignPromotedRoom.getCpcBid().compareTo(activePromotedRoom.getCpcBid()) > 0) {
//                    neededPromotedRoomIds.add(campaignPromotedRoom.getId());
//                    neededDeactivatedRoomIds.add(activePromotedRoom.getId());
//                    continue;
//                }
//            }
//            neededPromotedRoomIds.add(campaignPromotedRoom.getId());
//        }
//
//        promotedRoomRepository.updateStatusByIds(
//                PromotedRoomStatus.ACTIVE,
//                neededPromotedRoomIds
//        );
//        promotedRoomRepository.updateStatusByIds(
//                PromotedRoomStatus.PAUSED,
//                neededDeactivatedRoomIds
//        );
    }

    @Override
    public void expireCampaign(String campaignId) {
        AdCampaign campaign = adsCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", campaignId));
        if (campaign.getStatus() != AdCampaignStatus.ACTIVE) {
            return;
        }
        campaign.setStatus(AdCampaignStatus.COMPLETED);
        adsCampaignRepository.save(campaign);
        promotedRoomRepository.updateStatusByCampaignId(PromotedRoomStatus.PAUSED, campaignId);

        log.info("Campaign {} expired", campaignId);
    }

    private void reBudgetCampaign(AdCampaign campaign) {
        campaign.setStatus(AdCampaignStatus.ACTIVE);
        adsCampaignRepository.save(campaign);
    }

    private AdCampaign getCampaignAndValidateOwnership(String userId, String campaignId) {
        AdCampaign campaign = adsCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", campaignId));
        
        if (!campaign.getUser().getId().equals(userId)) {
            throw new APIException(HttpStatus.FORBIDDEN,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "You don't own this campaign");
        }
        
        return campaign;
    }
    
    private PromotedRoom getPromotedRoomAndValidateOwnership(String userId, String promotedRoomId) {
        PromotedRoom promotedRoom = promotedRoomRepository.findById(promotedRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Promoted Room", "id", promotedRoomId));
        
        if (!promotedRoom.getAdCampaign().getUser().getId().equals(userId)) {
            throw new APIException(HttpStatus.FORBIDDEN,
                                   ErrorCode.FLEXIBLE_ERROR,
                                   "You don't own this promoted room");
        }
        
        return promotedRoom;
    }

    private AdCampaignResponse mapToAdCampaignResponse(AdCampaign campaign) {
        CampaignStatistic campaignStatistic = campaign.getCampaignStatistics().get(0);
        long totalClicks = campaignStatistic.getClicks();
        long totalConversions = campaignStatistic.getConversionCount();
        long totalImpressions = campaignStatistic.getImpressionCount();

        Double clickThroughRate = totalImpressions > 0 ? (double) totalClicks / totalImpressions : 0.0;
        Double conversionRate = totalClicks > 0 ? (double) totalConversions / totalClicks : 0.0;
        Double costPerClick = totalClicks > 0 ? campaign.getSpentAmount().doubleValue() / totalClicks : 0.0;
        Double costPerMille = totalImpressions > 0 ?
                campaign.getSpentAmount().doubleValue() / (totalImpressions / 1000.0) : 0.0;
        
        CampaignStatisticsResponse statisticsResponse = CampaignStatisticsResponse.builder()
                .totalImpressions(totalImpressions)
                .totalClicks(totalClicks)
                .totalConversions(totalConversions)
                .totalSpent(campaign.getSpentAmount())
                .clickThroughRate(clickThroughRate)
                .conversionRate(conversionRate)
                .costPerClick(costPerClick)
                .costPerMille(costPerMille)
                .build();

        List<PromotedRoomResponse> promotedRoomResponses = new ArrayList<>();
        if (campaign.getPromotedRooms() != null && !campaign.getPromotedRooms().isEmpty()) {
            promotedRoomResponses = campaign.getPromotedRooms().stream()
                    .map(this::mapToPromotedRoomResponse)
                    .collect(Collectors.toList());
        }
        
        return AdCampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .pricingModel(campaign.getPricingModel().name())
                .status(campaign.getStatus())
                .budget(campaign.getBudget())
                .spentAmount(campaign.getSpentAmount())
                .dailyBudget(campaign.getDailyBudget())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .userId(campaign.getUser().getId())
                .promotedRooms(promotedRoomResponses)
                .statistics(statisticsResponse)
                .build();
    }
    
    private PromotedRoomResponse mapToPromotedRoomResponse(PromotedRoom promotedRoom) {
        return PromotedRoomResponse.builder()
                .id(promotedRoom.getId())
                .status(promotedRoom.getStatus())
                .bid(promotedRoom.getCpcBid())
                .adCampaignId(promotedRoom.getAdCampaign().getId())
                .roomId(promotedRoom.getRoom().getId())
                .build();
    }
}
