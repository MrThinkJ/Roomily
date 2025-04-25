package com.c2se.roomily.service.impl;

import com.c2se.roomily.payload.internal.PushNotificationDto;
import com.c2se.roomily.service.FCMService;
import com.c2se.roomily.service.PushNotificationService;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationServiceImpl implements PushNotificationService {
    private final FCMService fcmService;

    @Override
    public void sendPushNotificationToToken(PushNotificationDto pushNotificationRequestDto) throws FirebaseMessagingException{
        fcmService.sendMessageToToken(pushNotificationRequestDto);
        log.info("Push notification sent to token: " + pushNotificationRequestDto.getToken());
    }

    @Override
    public void sendPushNotificationToTopic(PushNotificationDto pushNotificationRequestDto) {
        fcmService.sendMessageWithoutData(pushNotificationRequestDto);
        log.info("Push notification sent to topic: " + pushNotificationRequestDto.getTopic());
    }
}
