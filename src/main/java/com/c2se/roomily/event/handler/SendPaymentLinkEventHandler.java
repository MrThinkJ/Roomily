package com.c2se.roomily.event.handler;

import com.c2se.roomily.entity.ChatMessage;
import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.enums.ChatRoomStatus;
import com.c2se.roomily.event.pojo.SendPaymentLinkEvent;
import com.c2se.roomily.payload.internal.CreateRentDepositPaymentLinkRequest;
import com.c2se.roomily.payload.request.CreatePaymentLinkRequest;
import com.c2se.roomily.payload.response.CheckoutResponse;
import com.c2se.roomily.service.ChatMessageService;
import com.c2se.roomily.service.ChatRoomService;
import com.c2se.roomily.service.PaymentProcessingService;
import com.c2se.roomily.service.RentedRoomService;
import com.c2se.roomily.util.UtilFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class SendPaymentLinkEventHandler {
    private final PaymentProcessingService paymentProcessingService;
    private final RentedRoomService rentedRoomService;
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @EventListener
    @Async
    public void handleSendPaymentLinkEvent(SendPaymentLinkEvent event) {
        RentedRoom rentedRoom = rentedRoomService.getRentedRoomEntityById(event.getRentedRoomId());
        ChatRoom chatRoom = chatRoomService.getChatRoomEntity(event.getChatRoomId());
        String requesterId = event.getRequesterId();

        String checkoutId = UtilFunction.hash(UUID.randomUUID().toString());
        CreateRentDepositPaymentLinkRequest createRentDepositPaymentLinkRequest = CreateRentDepositPaymentLinkRequest
                .builder()
                .amount(rentedRoom.getRoom().getRentalDeposit().intValue())
                .description("deposit")
                .productName("deposit")
                .rentedRoomId(rentedRoom.getId())
                .checkoutId(checkoutId)
                .chatMessageId(event.getChatMessageId())
                .build();
        log.info("Create payment link for rented room: {}", rentedRoom.getId());

        ChatMessage chatMessage = ChatMessage.builder()
                .message("Thanh toán tiền cọc qua QR")
                .chatRoom(chatRoom)
                .metadata(checkoutId)
                .subId(chatRoom.getNextSubId() + 1)
                .build();

        chatMessageService.saveSystemMessage(chatMessage, chatRoom);
        chatRoom.setStatus(ChatRoomStatus.COMPLETED);
        chatRoom.setLastMessage(chatMessage.getMessage());
        chatRoom.setLastMessageTimeStamp(LocalDateTime.now());
        chatRoom.setNextSubId(chatRoom.getNextSubId() + 1);
        chatRoom.setRentedRoomId(rentedRoom.getId());
        chatRoomService.saveChatRoom(chatRoom);
        paymentProcessingService.createPaymentLink(createRentDepositPaymentLinkRequest, requesterId);
        simpMessagingTemplate.convertAndSendToUser(requesterId, "/queue/chat-room",
                                                   chatRoom.getId());
    }
}
