package com.c2se.roomily.service;

import com.c2se.roomily.payload.internal.PushNotificationDto;
import com.google.firebase.messaging.FirebaseMessagingException;

public interface PushNotificationService {
    void sendPushNotificationToToken(PushNotificationDto pushNotificationRequestDto) throws FirebaseMessagingException;
    void sendPushNotificationToTopic(PushNotificationDto pushNotificationRequestDto);
}
