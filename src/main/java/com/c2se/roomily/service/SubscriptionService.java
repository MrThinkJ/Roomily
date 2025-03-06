package com.c2se.roomily.service;

import com.c2se.roomily.entity.Subscription;
import com.c2se.roomily.payload.request.UpdateSubscriptionRequest;
import com.c2se.roomily.payload.response.ActiveSubscriptionResponse;
import com.c2se.roomily.payload.response.UserSubscriptionResponse;

import java.util.List;

public interface SubscriptionService {
    void subscribe(String userId, String subscriptionId);

    void unsubscribe(String userId);

    void renewSubscription(String userId);

    List<Boolean> isSubscribed(String userId, List<String> subscriptionId);

    List<Subscription> getSubscriptions();

    ActiveSubscriptionResponse getActiveSubscription(String userId);

    List<UserSubscriptionResponse> getUserSubscriptions(String userId);
    List<String> getLandlordsWithActiveSubscriptions();
    boolean hasActiveSubscription(String landlordId);
    String getMostPopularSubscriptionId();

    void updateSubscription(String subscriptionId, UpdateSubscriptionRequest updateSubscriptionRequest);

    void deleteSubscription(String subscriptionId);
}
