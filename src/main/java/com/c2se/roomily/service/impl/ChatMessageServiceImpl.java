package com.c2se.roomily.service.impl;

import com.c2se.roomily.config.StorageConfig;
import com.c2se.roomily.entity.ChatMessage;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.ChatMessageToAdd;
import com.c2se.roomily.payload.response.ChatMessageResponse;
import com.c2se.roomily.repository.ChatMessageRepository;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.service.ChatMessageService;
import com.c2se.roomily.service.StorageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
    ChatMessageRepository chatMessageRepository;
    UserRepository userRepository;
    StorageService storageService;
    StorageConfig storageConfig;
    SimpMessagingTemplate messagingTemplate;

    @Override
    public ChatMessageResponse saveChatMessage(ChatMessageToAdd chatMessageToAdd) {
        User sender = userRepository.findById(chatMessageToAdd.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", chatMessageToAdd.getSenderId()));
        User recipient = userRepository.findById(chatMessageToAdd.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", chatMessageToAdd.getRecipientId()));
        String roomId = getChatRoomId(chatMessageToAdd.getSenderId(), chatMessageToAdd.getRecipientId());

        ChatMessage chatMessage = ChatMessage.builder()
                .sender(sender)
                .imageUrl(null)
                .recipient(recipient)
                .message(chatMessageToAdd.getContent())
                .roomId(roomId)
                .build();

        if (chatMessageToAdd.getImage() != null) {
            String fileName = roomId+ "_"+ UUID.randomUUID();
            try {
                storageService.putObject(chatMessageToAdd.getImage(), storageConfig.getBucketStore(),fileName);
                chatMessage.setImageUrl(storageService.generatePresignedUrl(storageConfig.getBucketStore(), fileName));
            } catch (Exception e) {
                throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR,
                        ErrorCode.FLEXIBLE_ERROR,
                        "Error while saving image");
            }
        }

        chatMessageRepository.save(chatMessage);
        messagingTemplate.convertAndSendToUser(chatMessage.getRecipient().getId(),
                "/queue/messages",
                chatMessage);
        return mapToResponse(chatMessage);
    }

    @Override
    public List<ChatMessageResponse> getChatMessages(String user1,
                                                     String user2,
                                                     String pivot, String timestamp,
                                                     int prev) {
        String roomId = getChatRoomId(user1, user2);
        List<ChatMessage> chatMessages;
        if (pivot == null && timestamp == null)
            chatMessages = chatMessageRepository.findLastedByRoomId(roomId, prev);
        else
            chatMessages = chatMessageRepository.findByRoomId(roomId, pivot, timestamp, prev);
        return chatMessages.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private ChatMessageResponse mapToResponse(ChatMessage chatMessage) {
        return ChatMessageResponse.builder()
                .id(chatMessage.getId())
                .senderId(chatMessage.getSender().getId())
                .recipientId(chatMessage.getRecipient().getId())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt().toString())
                .isRead(chatMessage.isRead())
                .imageUrl(chatMessage.getImageUrl())
                .build();
    }

    private String getChatRoomId(String user_1, String user_2) {
        int compare = user_1.compareTo(user_2);
        return compare < 0 ? user_1 + "_" + user_2 : user_2 + "_" + user_1;
    }
}
