package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.Notification;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.NotificationType;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.payload.response.NotificationResponse;
import com.c2se.roomily.repository.NotificationRepository;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    NotificationRepository notificationRepository;
    UserRepository userRepository;
    @Override
    public NotificationResponse getNotificationById(String id) {
        return mapToResponse(notificationRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Notification", "id", id)
        ));
    }

    @Override
    public List<NotificationResponse> getNotificationsByUser(String userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        return notifications.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getUnreadNotificationsByUser(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsRead(userId, false);
        return notifications.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getReadNotificationsByUser(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsRead(userId, true);
        return notifications.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public void markNotificationAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
                () -> new ResourceNotFoundException("Notification", "id", notificationId)
        );
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllNotificationsAsRead(String userId) {
        notificationRepository.markAllNotificationsAsRead(userId);
    }

    @Override
    public void sendNotification(CreateNotificationRequest request) {
        // TODO: Implement sending notification to device
        User user = userRepository.findById(request.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", request.getUserId())
        );
        Notification notification = Notification.builder()
                .header(request.getHeader())
                .body(request.getBody())
                .isRead(false)
                .type(NotificationType.valueOf(request.getType()))
                .user(user)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public void deleteNotificationById(String notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .header(notification.getHeader())
                .body(notification.getBody())
                .isRead(notification.getIsRead())
                .type(notification.getType().name())
                .createdAt(notification.getCreatedAt().toString())
                .userId(notification.getUser().getId())
                .build();
    }
}
