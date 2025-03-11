package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.Subscription;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.entity.UserSubscription;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.event.SubscriptionRenewalEvent;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.UpdateSubscriptionRequest;
import com.c2se.roomily.payload.response.ActiveSubscriptionResponse;
import com.c2se.roomily.payload.response.UserSubscriptionResponse;
import com.c2se.roomily.repository.SubscriptionRepository;
import com.c2se.roomily.repository.UserSubscriptionRepository;
import com.c2se.roomily.service.EventService;
import com.c2se.roomily.service.NotificationService;
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

    @Override
    public void subscribe(String userId, String subscriptionId) {
        if (userSubscriptionRepository.existsByUserIdAndSubscriptionIdAndEndDateAfter(
                userId, subscriptionId, LocalDateTime.now())) {
            throw new IllegalArgumentException("User already subscribed to this subscription");
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
                .orElseThrow(() -> new IllegalArgumentException("User not subscribed to this subscription"));
        if (userSubscription.isAutoRenew()) {
            throw new IllegalArgumentException("Subscription is already set to auto-renew");
        }
        userSubscription.setAutoRenew(true);
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
        UserSubscription userSubscription = userSubscriptionRepository.findByEndDateAfter(LocalDateTime.now())
                .orElse(null);

        if (userSubscription == null) {
            return null;
        }

        Subscription subscription = userSubscription.getSubscription();
        return ActiveSubscriptionResponse.builder()
                .subscriptionId(subscription.getId())
                .name(subscription.getName())
                .description(subscription.getDescription())
                .price(subscription.getPrice().toString())
                .duration(subscription.getDuration())
                .details(subscription.getDetails())
                .autoRenew(userSubscription.isAutoRenew())
                .subscriptionId(userSubscription.getSubscription().getId())
                .startDate(userSubscription.getStartDate())
                .endDate(userSubscription.getEndDate())
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
        subscriptionRepository.save(subscription);
    }

    @Override
    public void deleteSubscription(String subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId).orElseThrow(
                () -> new ResourceNotFoundException("Subscription", "id", subscriptionId));
        subscriptionRepository.delete(subscription);
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
                .subscriptionId(userSubscription.getSubscription().getId())
                .subscriptionName(userSubscription.getSubscription().getName())
                .subscriptionId(userSubscription.getSubscription().getId())
                .startDate(userSubscription.getStartDate())
                .endDate(userSubscription.getEndDate())
                .build();
    }
}
