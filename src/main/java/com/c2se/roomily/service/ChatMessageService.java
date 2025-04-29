package com.c2se.roomily.service;

import com.c2se.roomily.entity.ChatMessage;
import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.payload.request.ChatMessageToAdd;
import com.c2se.roomily.payload.response.ChatMessageResponse;

import java.util.List;

public interface ChatMessageService {
    ChatMessage getChatMessageById(String id);
    ChatMessage saveChatMessageEntity(ChatMessage chatMessage);
    ChatMessageResponse saveChatMessage(ChatMessageToAdd chatMessageToAdd);
    String saveSystemMessage(ChatMessage chatMessage, ChatRoom chatRoom);
    List<ChatMessageResponse> getChatMessages(String roomId, String userId, String pivot, String timestamp, int prev);
}
