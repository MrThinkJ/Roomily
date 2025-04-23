package com.c2se.roomily.service.impl;

import com.c2se.roomily.config.RabbitMQConfig;
import com.c2se.roomily.config.StorageConfig;
import com.c2se.roomily.entity.AdClickLog;
import com.c2se.roomily.entity.ChatMessage;
import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.event.pojo.AdConversionRecordEvent;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.ChatMessageToAdd;
import com.c2se.roomily.payload.response.ChatMessageResponse;
import com.c2se.roomily.repository.AdsConversionDeDupRepository;
import com.c2se.roomily.repository.ChatMessageRepository;
import com.c2se.roomily.repository.ChatRoomRepository;
import com.c2se.roomily.service.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
    private final UserService userService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;
    private final StorageService storageService;
    private final StorageConfig storageConfig;
    private final SimpMessagingTemplate messagingTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final AdsService adsService;
    private final AdsConversionDeDupRepository adsConversionDeDupRepository;

    @Override
    public ChatMessage getChatMessageById(String id) {
        return chatMessageRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Chat message", "id", id)
        );
    }

    @Override
    public void saveChatMessageEntity(ChatMessage chatMessage) {
        chatMessageRepository.save(chatMessage);
    }

    @Override
    public ChatMessageResponse saveChatMessage(ChatMessageToAdd chatMessageToAdd) {
        User sender = userService.getUserEntity(chatMessageToAdd.getSenderId());
        String chatRoomId = chatMessageToAdd.getChatRoomId();
        if (!chatRoomRepository.existsById(chatRoomId))
            throw new ResourceNotFoundException("Chat room", "id", chatRoomId);
        ChatRoom chatRoom = chatRoomRepository.findByIdLocked(chatRoomId).orElseThrow(
                () -> new ResourceNotFoundException("Chat room", "id", chatRoomId)
        );
        String campaignId = null;
        if (chatMessageToAdd.getIsAdConversion()){
            if (!adsConversionDeDupRepository.save(sender.getId(), chatRoomId)){
                throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                        "Ad conversion already exists for 15 minutes for this user in this chat room");
            }
            AdClickLog adClickLog = adsService.getAdClickLogById(chatMessageToAdd.getAdClickId());
            if (adClickLog == null) {
                throw new ResourceNotFoundException("Ad click log", "id", chatMessageToAdd.getAdClickId());
            }
            campaignId = adClickLog.getCampaignId();
            rabbitTemplate.convertAndSend(RabbitMQConfig.ADS_EXCHANGE_NAME,
                                          RabbitMQConfig.ADS_CONVERSION_ROUTING_KEY,
                                          AdConversionRecordEvent.builder()
                                                  .adClickId(chatMessageToAdd.getAdClickId())
                                                  .build());
        }
        ChatMessage chatMessage = ChatMessage.builder()
                .sender(sender)
                .imageUrl(null)
                .message(chatMessageToAdd.getContent())
                .chatRoom(chatRoom)
                .subId(chatRoom.getNextSubId() + 1)
                .roleName(sender.getRoles().stream().toList().get(0).getName())
                .adClickId(chatMessageToAdd.getAdClickId())
                .isRead(false)
                .adCampaignId(campaignId)
                .adClickId(chatMessageToAdd.getAdClickId())
                .isAdConversion(chatMessageToAdd.getIsAdConversion())
                .build();
        if (chatMessageToAdd.getImage() != null) {
            String fileName = chatRoomId + "_" + UUID.randomUUID();
            try {
                storageService.putObject(chatMessageToAdd.getImage(), storageConfig.getBucketStore(), fileName);
                chatMessage.setImageUrl(fileName);
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
        ChatMessageResponse response = mapToResponse(chatMessage);
        users.forEach(user -> messagingTemplate.convertAndSendToUser(user, "/queue/messages", response));
        return response;
    }

    @Override
    public String saveSystemMessage(ChatMessage chatMessage, ChatRoom chatRoom) {
        ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);
        List<String> users = chatRoomService.getChatRoomUserIds(chatRoom.getId());
        ChatMessageResponse response = mapToResponse(chatMessage);
        users.forEach(user -> messagingTemplate.convertAndSendToUser(user, "/queue/messages", response));
        return savedChatMessage.getId();
    }

    @Override
    public List<ChatMessageResponse> getChatMessages(String roomId, String userId, String pivot, String timestamp,
                                                     int prev) {
        List<ChatMessage> chatMessages;
        if (pivot == null && timestamp == null) chatMessages = chatMessageRepository.findLastedByRoomId(roomId, prev);
        else chatMessages = chatMessageRepository.findByRoomId(roomId, pivot, timestamp, prev);
        return chatMessages.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private ChatMessageResponse mapToResponse(ChatMessage chatMessage) {
        String url;
        try{
            url = chatMessage.getImageUrl() == null ? null :
                    storageService.generatePresignedUrl(storageConfig.getBucketStore(), chatMessage.getImageUrl());
        } catch (Exception e) {
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                   "Error while generating presigned url");
        }
        return ChatMessageResponse.builder()
                .id(chatMessage.getId())
                .senderId(chatMessage.getSender() == null ? null : chatMessage.getSender().getId())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt())
                .isRead(chatMessage.isRead())
                .imageUrl(url)
                .chatRoomId(chatMessage.getChatRoom().getId())
                .subId(chatMessage.getSubId())
                .metadata(chatMessage.getMetadata())
                .build();
    }
}
