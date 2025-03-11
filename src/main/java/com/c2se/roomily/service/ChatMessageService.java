package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.ChatMessageToAdd;
import com.c2se.roomily.payload.response.ChatMessageResponse;

import java.util.List;

public interface ChatMessageService {
    ChatMessageResponse saveChatMessage(ChatMessageToAdd chatMessageToAdd);
    ChatMessageResponse saveTestChatMessage(ChatMessageToAdd chatMessageToAdd);

    List<ChatMessageResponse> getChatMessages(String roomId, String userId, String pivot, String timestamp, int prev);
}
