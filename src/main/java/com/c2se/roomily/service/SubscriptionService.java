package com.c2se.roomily.service;

import com.c2se.roomily.entity.Subscription;
import com.c2se.roomily.payload.request.CreateRoomBoostRequest;
import com.c2se.roomily.payload.request.UpdateSubscriptionRequest;
import com.c2se.roomily.payload.response.ActiveSubscriptionResponse;
import com.c2se.roomily.payload.response.RoomBoostResponse;
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
    
    // Credit management methods
    Integer getRemainingCredits(String userId);
    
    void addCredits(String userId, Integer creditsToAdd);
    
    boolean useCredits(String userId, Integer creditsToUse);
    
    // Room boost methods
    RoomBoostResponse createRoomBoost(String userId, CreateRoomBoostRequest request);
    
    List<RoomBoostResponse> getUserRoomBoosts(String userId);
    
    RoomBoostResponse getRoomBoostById(String boostId);
    
    void deactivateRoomBoost(String boostId);
    
    List<RoomBoostResponse> getActiveRoomBoosts();
    
    List<String> getBoostedRoomIds();
    
    List<String> getBoostedRoomIdsByLocation(String city, String district, String ward);
    
    boolean isRoomBoosted(String roomId);
}
