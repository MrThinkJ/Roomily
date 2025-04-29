package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.payload.response.NotificationResponse;

import java.util.List;

public interface NotificationService {
    NotificationResponse getNotificationById(String id);

    List<NotificationResponse> getNotificationsByUser(String userId);

    List<NotificationResponse> getUnreadNotificationsByUser(String userId);

    List<NotificationResponse> getReadNotificationsByUser(String userId);

    void markNotificationAsRead(String notificationId);

    void markAllNotificationsAsRead(String userId);

    void sendNotification(CreateNotificationRequest request);

    void sendTestNotification(String toUserId, String message);

    void deleteNotificationById(String notificationId);
}
