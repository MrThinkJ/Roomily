package com.c2se.roomily.service;

import com.c2se.roomily.payload.internal.PushNotificationDto;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import java.util.Map;

public interface FCMService {
    void sendMessage(Map<String, String> data, PushNotificationDto request);
    void sendMessageWithoutData(PushNotificationDto request);
    void sendMessageToToken(PushNotificationDto request) throws FirebaseMessagingException;
    String sendAndGetResponse(Message message);
}
