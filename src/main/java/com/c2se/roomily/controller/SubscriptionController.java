package com.c2se.roomily.controller;

import com.c2se.roomily.entity.Subscription;
import com.c2se.roomily.payload.request.UpdateSubscriptionRequest;
import com.c2se.roomily.payload.response.ActiveSubscriptionResponse;
import com.c2se.roomily.payload.response.UserSubscriptionResponse;
import com.c2se.roomily.service.SubscriptionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

//    @GetMapping("/check")
//    public ResponseEntity<List<Boolean>> isSubscribed(@RequestBody List<String> subscriptionIds) {
//        String userId = getUserInfo().getId();
//        return ResponseEntity.ok(subscriptionService.isSubscribed(userId, subscriptionIds));
//    }

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
}
