package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.Notification;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.NotificationType;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.internal.PushNotificationDto;
import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.payload.response.NotificationResponse;
import com.c2se.roomily.repository.NotificationRepository;
import com.c2se.roomily.service.NotificationService;
import com.c2se.roomily.service.PushNotificationService;
import com.c2se.roomily.service.UserDeviceService;
import com.c2se.roomily.service.UserService;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final UserDeviceService userDeviceService;
    private final PushNotificationService pushNotificationService;

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
    public void sendNotification(CreateNotificationRequest createNotificationRequest) {
        User toUser = userService.getUserEntityById(createNotificationRequest.getUserId());
        List<String> deviceTokens = userDeviceService.getActiveUserToken(toUser.getId());
        log.info("Sending notification to user: "+toUser.getUsername());
        deviceTokens.forEach(token -> {
            try{
                PushNotificationDto request = PushNotificationDto.builder()
                        .title(createNotificationRequest.getHeader())
                        .message(createNotificationRequest.getBody())
                        .token(token)
                        .build();
                pushNotificationService.sendPushNotificationToToken(request);
                log.info("Notification sent to device: "+token);
            } catch (Exception e){
                if (e instanceof FirebaseMessagingException){
                    userDeviceService.deleteDevice(token);
                    log.info("Device deleted from database: "+token);
                }
                log.error("Error sending notification to device: "+token);
                log.error(e.getMessage());
            }
        });
        Notification notification = Notification.builder()
                .header(createNotificationRequest.getHeader())
                .body(createNotificationRequest.getBody())
                .isRead(false)
                .user(toUser)
                .build();
        notificationRepository.save(notification);
        log.info("Notification saved to database");
    }

    @Override
    public void sendTestNotification(String toUserId, String message) {
        User toUser = userService.getUserEntityById(toUserId);
        List<String> deviceTokens = userDeviceService.getActiveUserToken(toUser.getId());
        log.info("Sending test notification to user: "+toUser.getUsername());
        deviceTokens.forEach(token ->{
            try{
                PushNotificationDto request = PushNotificationDto.builder()
                        .title("Test Notification")
                        .message(message)
                        .token(token)
                        .build();
                pushNotificationService.sendPushNotificationToToken(request);
                log.info("Test notification sent to device: "+token);
            } catch (Exception e){
                if (e instanceof FirebaseMessagingException){
                    userDeviceService.deleteDevice(token);
                    log.info("Device deleted from database: "+token);
                }
                log.error("Error sending test notification to device: "+token);
                log.error(e.getMessage());
            }
        });
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
                .createdAt(notification.getCreatedAt())
                .userId(notification.getUser().getId())
                .build();
    }
}
