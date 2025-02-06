package com.c2se.roomily.controller;

import com.c2se.roomily.payload.response.NotificationResponse;
import com.c2se.roomily.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController extends BaseController{
    NotificationService notificationService;
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

    @GetMapping("/mark/{notificationId}")
    public ResponseEntity<Boolean> markNotificationAsRead(@PathVariable String notificationId) {
        notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/mark/all")
    public ResponseEntity<Boolean> markAllNotificationsAsRead() {
        String userId = this.getUserInfo().getId();
        notificationService.markAllNotificationsAsRead(userId);
        return ResponseEntity.ok(true);
    }
}
