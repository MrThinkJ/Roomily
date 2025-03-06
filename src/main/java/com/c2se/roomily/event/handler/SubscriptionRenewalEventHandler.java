package com.c2se.roomily.event.handler;

import com.c2se.roomily.entity.Subscription;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.entity.UserSubscription;
import com.c2se.roomily.event.SubscriptionRenewalEvent;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.repository.UserSubscriptionRepository;
import com.c2se.roomily.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

@Component
@AllArgsConstructor
@Slf4j
public class SubscriptionRenewalEventHandler {
    UserRepository userRepository;
    UserSubscriptionRepository userSubscriptionRepository;
    NotificationService notificationService;
    TaskScheduler taskScheduler;

    @EventListener
    @Async
    public void handleSubscriptionRenewal(SubscriptionRenewalEvent event) {
        Runnable renewalTask = () -> processRenewal(event.getSubscriptionId());
        taskScheduler.schedule(renewalTask,
                event.getRenewalTime().atZone(ZoneId.systemDefault()).toInstant());
    }

    @Transactional(rollbackFor = Exception.class)
    protected void processRenewal(String subscriptionId) {
        try {
            UserSubscription subscription = userSubscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new ResourceNotFoundException("UserSubscription", "id", subscriptionId));
            if (!subscription.isAutoRenew()) {
                return;
            }
            User user = subscription.getUser();
            Subscription subDetails = subscription.getSubscription();

            if (user.getBalance().compareTo(subDetails.getPrice()) >= 0) {
                user.setBalance(user.getBalance().subtract(subDetails.getPrice()));
                userRepository.save(user);

                subscription.setStartDate(subscription.getEndDate());
                subscription.setEndDate(subscription.getEndDate().plusMonths(subDetails.getDuration()));
                userSubscriptionRepository.save(subscription);

                notificationService.sendNotification(CreateNotificationRequest.builder()
                        .header("Subscription Renewed")
                        .body("Your subscription has been automatically renewed.")
                        .userId(user.getId())
                        .type("SUBSCRIPTION")
                        .build());
            } else {
                subscription.setAutoRenew(false);
                userSubscriptionRepository.save(subscription);
                notificationService.sendNotification(CreateNotificationRequest.builder()
                        .header("Subscription Renewal Failed")
                        .body("Auto-renewal failed due to insufficient balance.")
                        .userId(user.getId())
                        .type("SUBSCRIPTION")
                        .build());
            }
        } catch (Exception e) {
            log.error("Failed to process renewal for subscription: " + subscriptionId, e);
        }
    }
}