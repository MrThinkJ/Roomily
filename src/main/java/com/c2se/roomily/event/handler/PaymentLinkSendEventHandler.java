package com.c2se.roomily.event.handler;

import com.c2se.roomily.config.StorageConfig;
import com.c2se.roomily.entity.ChatMessage;
import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.enums.ChatRoomStatus;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.event.pojo.SendPaymentLinkEvent;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.payload.internal.CreateRentDepositPaymentLinkRequest;
import com.c2se.roomily.payload.response.ChatMessageResponse;
import com.c2se.roomily.service.*;
import com.c2se.roomily.util.UtilFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentLinkSendEventHandler {
    private final PaymentProcessingService paymentProcessingService;
    private final RentedRoomService rentedRoomService;
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final StorageService storageService;
    private final StorageConfig storageConfig;

    @EventListener
    @Async
    public void handleSendPaymentLinkEvent(SendPaymentLinkEvent event) {
        RentedRoom rentedRoom = rentedRoomService.getRentedRoomEntityById(event.getRentedRoomId());
        ChatRoom chatRoom = chatRoomService.getChatRoomEntity(event.getChatRoomId());
        String requesterId = event.getRequesterId();

        String checkoutId = UtilFunction.hash(UUID.randomUUID().toString());
        log.info("Create payment link for rented room: {}", rentedRoom.getId());
        ChatMessage chatMessage = ChatMessage.builder()
                .message("Thanh toán tiền cọc qua QR")
                .chatRoom(chatRoom)
                .metadata(checkoutId)
                .subId(chatRoom.getNextSubId() + 1)
                .build();
        ChatMessage savedChatMessage = chatMessageService.saveChatMessageEntity(chatMessage);
        List<String> users = chatRoomService.getChatRoomUserIds(chatRoom.getId());
        ChatMessageResponse response = mapToResponse(chatMessage);
        CreateRentDepositPaymentLinkRequest createRentDepositPaymentLinkRequest =
                CreateRentDepositPaymentLinkRequest.builder()
                .amount(rentedRoom.getRoom().getRentalDeposit().intValue())
                .description("deposit")
                .productName("deposit")
                .rentedRoomId(rentedRoom.getId())
                .checkoutId(checkoutId)
                .chatMessageId(savedChatMessage.getId())
                .build();
        paymentProcessingService.createPaymentLink(createRentDepositPaymentLinkRequest, requesterId);
        chatRoom.setStatus(ChatRoomStatus.COMPLETED);
        chatRoom.setLastMessage(chatMessage.getMessage());
        chatRoom.setLastMessageTimeStamp(LocalDateTime.now());
        chatRoom.setNextSubId(chatRoom.getNextSubId() + 1);
        chatRoom.setRentedRoomId(rentedRoom.getId());
        chatRoomService.saveChatRoom(chatRoom);
        users.forEach(user -> simpMessagingTemplate.convertAndSendToUser(user, "/queue/messages", response));
        simpMessagingTemplate.convertAndSendToUser(requesterId, "/queue/chat-room",
                                                   chatRoom.getId());
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
