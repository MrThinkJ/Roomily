package com.c2se.roomily.controller;

import com.c2se.roomily.payload.response.NotificationResponse;
import com.c2se.roomily.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController extends BaseController {
    private final NotificationService notificationService;

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable String id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUser() {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(notificationService.getNotificationsByUser(userId));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotificationsByUser() {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(notificationService.getUnreadNotificationsByUser(userId));
    }

    @GetMapping("/read")
    public ResponseEntity<List<NotificationResponse>> getReadNotificationsByUser() {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(notificationService.getReadNotificationsByUser(userId));
    }

    @PostMapping("/mark/{notificationId}")
    public ResponseEntity<Boolean> markNotificationAsRead(@PathVariable String notificationId) {
        notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/mark/all")
    public ResponseEntity<Boolean> markAllNotificationsAsRead() {
        String userId = this.getUserInfo().getId();
        notificationService.markAllNotificationsAsRead(userId);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/test/{userId}")
    public ResponseEntity<Boolean> test(@PathVariable String userId) {
        notificationService.sendTestNotification(userId, "Test Notification");
        return ResponseEntity.ok(true);
    }
}
