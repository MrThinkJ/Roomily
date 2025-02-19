package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.ChatMessageToAdd;
import com.c2se.roomily.payload.response.ChatMessageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatMessageService {
    ChatMessageResponse saveChatMessage(ChatMessageToAdd chatMessageToAdd);
    List<ChatMessageResponse> getChatMessages(String user1, String user2,
                                              String pivot, String timestamp,
                                              int prev);
}
