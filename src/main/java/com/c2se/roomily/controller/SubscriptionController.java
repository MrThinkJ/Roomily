package com.c2se.roomily.controller;

import com.c2se.roomily.entity.Subscription;
import com.c2se.roomily.payload.request.CreateRoomBoostRequest;
import com.c2se.roomily.payload.request.UpdateSubscriptionRequest;
import com.c2se.roomily.payload.request.UseCreditsRequest;
import com.c2se.roomily.payload.response.ActiveSubscriptionResponse;
import com.c2se.roomily.payload.response.RoomBoostResponse;
import com.c2se.roomily.payload.response.UserSubscriptionResponse;
import com.c2se.roomily.service.SubscriptionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController extends BaseController {
    SubscriptionService subscriptionService;

    @PostMapping("/subscribe/{subscriptionId}")
    public ResponseEntity<Void> subscribe(@PathVariable String subscriptionId) {
        String userId = getUserInfo().getId();
        subscriptionService.subscribe(userId, subscriptionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe() {
        String userId = getUserInfo().getId();
        subscriptionService.unsubscribe(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/renew")
    public ResponseEntity<Void> renewSubscription() {
        String userId = getUserInfo().getId();
        subscriptionService.renewSubscription(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/credits")
    public ResponseEntity<Map<String, Integer>> getRemainingCredits() {
        String userId = getUserInfo().getId();
        int credits = subscriptionService.getRemainingCredits(userId);
        return ResponseEntity.ok(Map.of("credits", credits));
    }

    @PostMapping("/credits/use")
    public ResponseEntity<?> useCredits(@RequestBody UseCreditsRequest useCreditsRequest) {
        String userId = getUserInfo().getId();
        boolean success = subscriptionService.useCredits(userId, useCreditsRequest.getCreditsAmount());

        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Not enough credits",
                "required", useCreditsRequest.getCreditsAmount(),
                "available", subscriptionService.getRemainingCredits(userId)
            ));
        }
    }

    @PostMapping("/credits/add")
    public ResponseEntity<Void> addCredits(@RequestParam int amount) {
        String userId = getUserInfo().getId();
        subscriptionService.addCredits(userId, amount);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Subscription>> getSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getSubscriptions());
    }

    @GetMapping("/active")
    public ResponseEntity<ActiveSubscriptionResponse> getActiveSubscription() {
        String userId = getUserInfo().getId();
        return ResponseEntity.ok(subscriptionService.getActiveSubscription(userId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<UserSubscriptionResponse>> getUserSubscriptions() {
        String userId = getUserInfo().getId();
        return ResponseEntity.ok(subscriptionService.getUserSubscriptions(userId));
    }

    @GetMapping("/popular")
    public ResponseEntity<String> getMostPopularSubscriptionId() {
        return ResponseEntity.ok(subscriptionService.getMostPopularSubscriptionId());
    }

    @PutMapping("/{subscriptionId}")
    public ResponseEntity<Void> updateSubscription(
            @PathVariable String subscriptionId,
            @RequestBody UpdateSubscriptionRequest updateSubscriptionRequest) {
        subscriptionService.updateSubscription(subscriptionId, updateSubscriptionRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable String subscriptionId) {
        subscriptionService.deleteSubscription(subscriptionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/boosts")
    public ResponseEntity<RoomBoostResponse> createRoomBoost(@RequestBody CreateRoomBoostRequest request) {
        String userId = getUserInfo().getId();
        return ResponseEntity.ok(subscriptionService.createRoomBoost(userId, request));
    }

    @GetMapping("/boosts")
    public ResponseEntity<List<RoomBoostResponse>> getUserRoomBoosts() {
        String userId = getUserInfo().getId();
        return ResponseEntity.ok(subscriptionService.getUserRoomBoosts(userId));
    }

    @GetMapping("/boosts/{boostId}")
    public ResponseEntity<RoomBoostResponse> getRoomBoostById(@PathVariable String boostId) {
        return ResponseEntity.ok(subscriptionService.getRoomBoostById(boostId));
    }

    @PostMapping("/boosts/{boostId}/deactivate")
    public ResponseEntity<Void> deactivateRoomBoost(@PathVariable String boostId) {
        subscriptionService.deactivateRoomBoost(boostId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/boosts/active")
    public ResponseEntity<List<RoomBoostResponse>> getActiveRoomBoosts() {
        return ResponseEntity.ok(subscriptionService.getActiveRoomBoosts());
    }

    @GetMapping("/boosts/rooms")
    public ResponseEntity<List<String>> getBoostedRoomIds() {
        return ResponseEntity.ok(subscriptionService.getBoostedRoomIds());
    }

//    @GetMapping("/boosts/rooms/location")
//    public ResponseEntity<List<String>> getBoostedRoomIdsByLocation(
//            @RequestParam(required = false) String city,
//            @RequestParam(required = false) String district,
//            @RequestParam(required = false) String ward) {
//        return ResponseEntity.ok(subscriptionService.getBoostedRoomIdsByLocation(city, district, ward));
//    }

    @GetMapping("/boosts/rooms/{roomId}/status")
    public ResponseEntity<Boolean> isRoomBoosted(@PathVariable String roomId) {
        return ResponseEntity.ok(subscriptionService.isRoomBoosted(roomId));
    }
}
