package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.*;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.event.pojo.SubscriptionRenewalEvent;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.CreateRoomBoostRequest;
import com.c2se.roomily.payload.request.UpdateSubscriptionRequest;
import com.c2se.roomily.payload.response.ActiveSubscriptionResponse;
import com.c2se.roomily.payload.response.RoomBoostResponse;
import com.c2se.roomily.payload.response.UserSubscriptionResponse;
import com.c2se.roomily.repository.RoomBoostRepository;
import com.c2se.roomily.repository.SubscriptionRepository;
import com.c2se.roomily.repository.UserSubscriptionRepository;
import com.c2se.roomily.service.EventService;
import com.c2se.roomily.service.NotificationService;
import com.c2se.roomily.service.RoomService;
import com.c2se.roomily.service.SubscriptionService;
import com.c2se.roomily.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    UserService userService;
    SubscriptionRepository subscriptionRepository;
    NotificationService notificationService;
    UserSubscriptionRepository userSubscriptionRepository;
    EventService eventService;
    RoomService roomService;
    RoomBoostRepository roomBoostRepository;

    @Override
    public void subscribe(String userId, String subscriptionId) {
        if (userSubscriptionRepository.existsByUserIdAndEndDateAfter(
                userId, LocalDateTime.now())) {
            throw new IllegalArgumentException("User already has an active subscription");
        }

        User user = userService.getUserEntity(userId);
        Subscription subscription = subscriptionRepository.findById(subscriptionId).orElseThrow(
                () -> new ResourceNotFoundException("Subscription", "id", subscriptionId));

        if (user.getBalance().compareTo(subscription.getPrice()) < 0) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.INSUFFICIENT_BALANCE,
                                   subscription.getPrice(),
                                   user.getBalance());
        }

        UserSubscription userSubscription = userSubscriptionRepository.findByUserIdAndEndDateAfter(
                        userId, LocalDateTime.now())
                .orElse(UserSubscription.builder()
                                .user(user)
                                .subscription(subscription)
                                .startDate(LocalDateTime.now())
                                .endDate(LocalDateTime.now().plusMonths(subscription.getDuration()))
                                .autoRenew(true)
                                .remainingCredits(subscription.getCredits())
                                .build());

        user.setBalance(user.getBalance().subtract(subscription.getPrice()));
        userSubscriptionRepository.save(userSubscription);
    }

    @Override
    public void unsubscribe(String userId) {
        UserSubscription userSubscription = userSubscriptionRepository.findByUserIdAndEndDateAfter(userId,
                                                                                                   LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("User not subscribed to this subscription"));
        userSubscription.setAutoRenew(false);
        userSubscriptionRepository.save(userSubscription);
    }

    @Override
    public void renewSubscription(String userId) {
        UserSubscription userSubscription = userSubscriptionRepository.findByUserIdAndEndDateAfter(userId,
                                                                                                   LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("User does not have an active subscription"));
        User user = userService.getUserEntity(userId);
        Subscription subscription = userSubscription.getSubscription();

        if (user.getBalance().compareTo(subscription.getPrice()) < 0) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                                   ErrorCode.INSUFFICIENT_BALANCE,
                                   subscription.getPrice(),
                                   user.getBalance());
        }

        userSubscription.setEndDate(userSubscription.getEndDate().plusMonths(subscription.getDuration()));
        userSubscription.setRemainingCredits(userSubscription.getRemainingCredits() + subscription.getCredits());
        user.setBalance(user.getBalance().subtract(subscription.getPrice()));
        userSubscriptionRepository.save(userSubscription);
    }

    @Override
    public List<Boolean> isSubscribed(String userId, List<String> subscriptionId) {
        return subscriptionId.stream()
                .map(id -> userSubscriptionRepository.existsByUserIdAndSubscriptionIdAndEndDateAfter(
                        userId, id, LocalDateTime.now()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Subscription> getSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @Override
    public ActiveSubscriptionResponse getActiveSubscription(String userId) {
        UserSubscription userSubscription = userSubscriptionRepository.findByUserIdAndEndDateAfter(userId,
                                                                                                   LocalDateTime.now())
                .orElse(null);

        if (userSubscription == null) {
            return null;
        }

        Subscription subscription = userSubscription.getSubscription();
        return ActiveSubscriptionResponse.builder()
                .id(userSubscription.getId())
                .subscriptionId(subscription.getId())
                .name(subscription.getName())
                .description(subscription.getDescription())
                .price(subscription.getPrice().toString())
                .duration(subscription.getDuration())
                .details(subscription.getDetails())
                .autoRenew(userSubscription.isAutoRenew())
                .startDate(userSubscription.getStartDate())
                .endDate(userSubscription.getEndDate())
                .remainingCredits(userSubscription.getRemainingCredits())
                .build();
    }

    @Override
    public List<UserSubscriptionResponse> getUserSubscriptions(String userId) {
        List<UserSubscription> userSubscriptions = userSubscriptionRepository.findByUserId(userId);
        return userSubscriptions.stream().map(this::mapToUserSubscriptionResponse).collect(Collectors.toList());
    }

    @Override
    public List<String> getLandlordsWithActiveSubscriptions() {
        // TODO: Cache this
        return userSubscriptionRepository.findLandlordsWithActiveSubscriptions();
    }

    @Override
    public boolean hasActiveSubscription(String landlordId) {
        return userSubscriptionRepository.hasActiveSubscription(landlordId);
    }

    @Override
    public String getMostPopularSubscriptionId() {
        LocalDateTime now = LocalDateTime.now();
        return userSubscriptionRepository.findMostPopularActiveSubscriptionId(now)
                .orElse(null);
    }

    @Override
    public void updateSubscription(String subscriptionId, UpdateSubscriptionRequest updateSubscriptionRequest) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId).orElseThrow(
                () -> new ResourceNotFoundException("Subscription", "id", subscriptionId));
        subscription.setPrice(BigDecimal.valueOf(Double.parseDouble(updateSubscriptionRequest.getPrice())));
        subscription.setName(updateSubscriptionRequest.getName());
        subscription.setDescription(updateSubscriptionRequest.getDescription());
        subscription.setDuration(updateSubscriptionRequest.getDuration());
        subscription.setCredits(updateSubscriptionRequest.getIncludedCredits());
        subscriptionRepository.save(subscription);
    }

    @Override
    public void deleteSubscription(String subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId).orElseThrow(
                () -> new ResourceNotFoundException("Subscription", "id", subscriptionId));
        subscriptionRepository.delete(subscription);
    }

    @Override
    public Integer getRemainingCredits(String userId) {
        UserSubscription userSubscription = userSubscriptionRepository.findByUserIdAndEndDateAfter(
                userId, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("User does not have an active subscription"));
        return userSubscription.getRemainingCredits();
    }

    @Override
    public void addCredits(String userId, Integer creditsToAdd) {
        if (creditsToAdd <= 0) {
            throw new IllegalArgumentException("Credits to add must be greater than 0");
        }
        
        UserSubscription userSubscription = userSubscriptionRepository.findByUserIdAndEndDateAfter(
                userId, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("User does not have an active subscription"));
        
        userSubscription.setRemainingCredits(userSubscription.getRemainingCredits() + creditsToAdd);
        userSubscriptionRepository.save(userSubscription);
    }

    @Override
    public boolean useCredits(String userId, Integer creditsToUse) {
        // TODO: Re-Implement
        if (creditsToUse <= 0) {
            throw new IllegalArgumentException("Credits to use must be greater than 0");
        }
        
        UserSubscription userSubscription = userSubscriptionRepository.findByUserIdAndEndDateAfter(
                userId, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("User does not have an active subscription"));
        
        if (userSubscription.getRemainingCredits() < creditsToUse) {
            return false;
        }
        
        userSubscription.setRemainingCredits(userSubscription.getRemainingCredits() - creditsToUse);
        userSubscriptionRepository.save(userSubscription);
        return true;
    }

    @Override
    public RoomBoostResponse createRoomBoost(String userId, CreateRoomBoostRequest request) {
        // TODO: Calculate credits needed based on boost level
        // Check if user has enough credits
        UserSubscription userSubscription = userSubscriptionRepository.findByUserIdAndEndDateAfter(
                userId, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("User does not have an active subscription"));
        
        if (userSubscription.getRemainingCredits() < request.getCreditsToUse()) {
            throw new APIException(HttpStatus.BAD_REQUEST, 
                                  ErrorCode.INSUFFICIENT_BALANCE, 
                                  request.getCreditsToUse(), 
                                  userSubscription.getRemainingCredits());
        }
        
        // Verify room exists and belongs to the user
        Room room = roomService.getRoomEntityById(request.getRoomId());
        if (!room.getLandlord().getId().equals(userId)) {
            throw new IllegalArgumentException("Room does not belong to the user");
        }
        
        // Create the boost
        RoomBoost boost = RoomBoost.builder()
                .room(room)
                .user(userService.getUserEntity(userId))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .creditsUsed(request.getCreditsToUse())
                .active(true)
                .boostLevel(request.getBoostLevel())
                .radiusKm(request.getRadiusKm())
                .build();
        
        // Deduct credits
        userSubscription.setRemainingCredits(userSubscription.getRemainingCredits() - request.getCreditsToUse());
        userSubscriptionRepository.save(userSubscription);
        
        // Save boost
        RoomBoost savedBoost = roomBoostRepository.save(boost);
        
        return mapToRoomBoostResponse(savedBoost);
    }

    @Override
    public List<RoomBoostResponse> getUserRoomBoosts(String userId) {
        List<RoomBoost> boosts = roomBoostRepository.findByUserId(userId);
        return boosts.stream()
                .map(this::mapToRoomBoostResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoomBoostResponse getRoomBoostById(String boostId) {
        RoomBoost boost = roomBoostRepository.findById(boostId)
                .orElseThrow(() -> new ResourceNotFoundException("Room Boost", "id", boostId));
        return mapToRoomBoostResponse(boost);
    }

    @Override
    public void deactivateRoomBoost(String boostId) {
        RoomBoost boost = roomBoostRepository.findById(boostId)
                .orElseThrow(() -> new ResourceNotFoundException("Room Boost", "id", boostId));
        boost.setActive(false);
        roomBoostRepository.save(boost);
        
        // Optionally, refund unused credits based on time remaining
        // This would require tracking used vs allocated credits which is not implemented in this version
    }

    @Override
    public List<RoomBoostResponse> getActiveRoomBoosts() {
        List<RoomBoost> boosts = roomBoostRepository.findActiveBoosts(LocalDateTime.now());
        return boosts.stream()
                .map(this::mapToRoomBoostResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getBoostedRoomIds() {
        return roomBoostRepository.findActiveBoostedRoomIds(LocalDateTime.now());
    }

    @Override
    public List<String> getBoostedRoomIdsByLocation(String city, String district, String ward) {
//        return roomBoostRepository.findActiveBoostedRoomIdsByLocation(city, district, ward, LocalDateTime.now());
        return null;
    }

    @Override
    public boolean isRoomBoosted(String roomId) {
        return roomBoostRepository.isRoomBoosted(roomId, LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional(readOnly = true)
    public void scheduleRenewalsForToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<UserSubscription> todayRenewals = userSubscriptionRepository
                .findByEndDateBetweenAndAutoRenewIsTrue(startOfDay, endOfDay);

        todayRenewals.forEach(subscription ->
                                      eventService.publishEvent(SubscriptionRenewalEvent.builder()
                                                                        .renewalTime(subscription.getEndDate())
                                                                        .subscriptionId(subscription.getId())
                                                                        .build())
        );
    }

    private UserSubscriptionResponse mapToUserSubscriptionResponse(UserSubscription userSubscription) {
        return UserSubscriptionResponse.builder()
                .id(userSubscription.getId())
                .subscriptionId(userSubscription.getSubscription().getId())
                .subscriptionName(userSubscription.getSubscription().getName())
                .startDate(userSubscription.getStartDate())
                .endDate(userSubscription.getEndDate())
                .remainingCredits(userSubscription.getRemainingCredits())
                .build();
    }
    
    private RoomBoostResponse mapToRoomBoostResponse(RoomBoost boost) {
        return RoomBoostResponse.builder()
                .id(boost.getId())
                .roomId(boost.getRoom().getId())
                .roomTitle(boost.getRoom().getTitle())
                .roomAddress(boost.getRoom().getAddress())
                .userId(boost.getUser().getId())
                .userName(boost.getUser().getFullName())
                .startDate(boost.getStartDate())
                .endDate(boost.getEndDate())
                .creditsUsed(boost.getCreditsUsed())
                .active(boost.isActive())
                .boostLevel(boost.getBoostLevel())
                .radiusKm(boost.getRadiusKm())
                .createdAt(boost.getCreatedAt())
                .updatedAt(boost.getUpdatedAt())
                .build();
    }
}
