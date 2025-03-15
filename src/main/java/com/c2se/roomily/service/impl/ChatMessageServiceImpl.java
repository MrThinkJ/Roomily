package com.c2se.roomily.service.impl;

import com.c2se.roomily.config.StorageConfig;
import com.c2se.roomily.entity.ChatMessage;
import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.ChatMessageToAdd;
import com.c2se.roomily.payload.response.ChatMessageResponse;
import com.c2se.roomily.repository.ChatMessageRepository;
import com.c2se.roomily.repository.ChatRoomRepository;
import com.c2se.roomily.service.ChatMessageService;
import com.c2se.roomily.service.ChatRoomService;
import com.c2se.roomily.service.StorageService;
import com.c2se.roomily.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
    ChatMessageRepository chatMessageRepository;
    UserService userService;
    ChatRoomRepository chatRoomRepository;
    ChatRoomService chatRoomService;
    StorageService storageService;
    StorageConfig storageConfig;
    SimpMessagingTemplate messagingTemplate;

    @Override
    public ChatMessageResponse saveChatMessage(ChatMessageToAdd chatMessageToAdd) {
        User sender = userService.getUserEntity(chatMessageToAdd.getSenderId());
        String chatRoomId = chatMessageToAdd.getChatRoomId();
        if (!chatRoomRepository.existsById(chatRoomId))
            throw new ResourceNotFoundException("Chat room", "id", chatRoomId);
        ChatRoom chatRoom = chatRoomRepository.findByIdLocked(chatRoomId).orElseThrow(
                () -> new ResourceNotFoundException("Chat room", "id", chatRoomId)
        );
        ChatMessage chatMessage = ChatMessage.builder()
                .sender(sender)
                .imageUrl(null)
                .message(chatMessageToAdd.getContent())
                .chatRoom(chatRoom)
                .subId(chatRoom.getNextSubId() + 1)
                .build();
        if (chatMessageToAdd.getImage() != null) {
            String fileName = chatRoomId + "_" + UUID.randomUUID();
            try {
                storageService.putObject(chatMessageToAdd.getImage(), storageConfig.getBucketStore(), fileName);
                chatMessage.setImageUrl(storageService.generatePresignedUrl(storageConfig.getBucketStore(), fileName));
            } catch (Exception e) {
                throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                       "Error while saving image");
            }
        }
        chatRoom.setLastMessage(chatMessage.getMessage());
        chatRoom.setLastMessageTimeStamp(chatMessage.getCreatedAt());
        chatRoom.setLastMessageSender(chatMessage.getSender().getId());
        chatRoom.setNextSubId(chatRoom.getNextSubId() + 1);
        chatRoomRepository.save(chatRoom);
        chatMessageRepository.save(chatMessage);
        List<String> users = chatRoomService.getChatRoomUserIds(chatRoomId);
        users.forEach(user -> messagingTemplate.convertAndSendToUser(user, "/queue/messages", chatMessage));
        return mapToResponse(chatMessage);
    }

    @Override
    public ChatMessageResponse saveTestChatMessage(ChatMessageToAdd chatMessageToAdd) {
        User sender = userService.getUserEntity(chatMessageToAdd.getSenderId());
        String roomId = chatMessageToAdd.getChatRoomId();
        ChatMessage chatMessage = ChatMessage.builder()
                .sender(sender)
                .imageUrl(null)
                .message(chatMessageToAdd.getContent())
//                .chatRoom(roomId)
                .subId(0)
                .build();
        if (chatMessageToAdd.getImage() != null) {
            String fileName = roomId + "_" + UUID.randomUUID();
            try {
                storageService.putObject(chatMessageToAdd.getImage(), storageConfig.getBucketStore(), fileName);
                chatMessage.setImageUrl(storageService.generatePresignedUrl(storageConfig.getBucketStore(), fileName));
            } catch (Exception e) {
                throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                       "Error while saving image");
            }
        }
        messagingTemplate.convertAndSendToUser("70f70be9-fd3a-4314-85c7-8e3881d8579a",
                                               "/queue/messages",
                                               chatMessage);
        return mapToResponse(chatMessage);
    }

    @Override
    public List<ChatMessageResponse> getChatMessages(String userId, String roomId, String pivot, String timestamp,
                                                     int prev) {
        String[] users = roomId.split("_");
        if (!users[1].equals(userId) && !users[2].equals(userId))
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "Invalid room id");
        List<ChatMessage> chatMessages;
        if (pivot == null && timestamp == null) chatMessages = chatMessageRepository.findLastedByRoomId(roomId, prev);
        else chatMessages = chatMessageRepository.findByRoomId(roomId, pivot, timestamp, prev);
        return chatMessages.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private ChatMessageResponse mapToResponse(ChatMessage chatMessage) {
        return ChatMessageResponse.builder().id(chatMessage.getId()).senderId(chatMessage.getSender().getId()).message(
                chatMessage.getMessage()).createdAt(chatMessage.getCreatedAt()).isRead(chatMessage.isRead()).imageUrl(
                chatMessage.getImageUrl()).build();
    }
}
