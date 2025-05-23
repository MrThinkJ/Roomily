package com.c2se.roomily.event.handler;

import com.c2se.roomily.entity.ChatMessage;
import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ChatRoomStatus;
import com.c2se.roomily.enums.RentedRoomStatus;
import com.c2se.roomily.event.pojo.DepositPayEvent;
import com.c2se.roomily.event.pojo.SendPaymentLinkEvent;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.payload.request.CreatePaymentLinkRequest;
import com.c2se.roomily.payload.response.CheckoutResponse;
import com.c2se.roomily.repository.RentedRoomRepository;
import com.c2se.roomily.service.*;
import com.c2se.roomily.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositPayEventHandler {
    private final RentedRoomRepository rentedRoomRepository;
    private final PaymentProcessingService paymentProcessingService;
    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final TaskScheduler taskScheduler;
    private final EventService eventService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleDepositPayEvent(DepositPayEvent event) {
        String rentedRoomId = event.getRentedRoomId();
        ChatRoom chatRoom = event.getChatRoom();
        String requesterId = event.getRequesterId();
        String taskId = "deposit-pay-" + rentedRoomId;
        sendSystemMessage(rentedRoomId, chatRoom, requesterId);
        eventService.publishEvent(SendPaymentLinkEvent.builder(this)
                                          .rentedRoomId(rentedRoomId)
                                          .chatRoomId(chatRoom.getId())
                                          .requesterId(requesterId)
                                          .build());
        log.info("Processing deposit pay event for rented room ID: {}", rentedRoomId);
        publishTask(rentedRoomId);
    }

    private void publishTask(String rentedRoomId){
        try {
            Runnable task = () -> {
                RentedRoom rr = rentedRoomRepository.findById(rentedRoomId).orElseThrow(
                        () -> new ResourceNotFoundException("Rented room", "id", rentedRoomId)
                );
                if (rr.getStatus().equals(RentedRoomStatus.DEPOSIT_NOT_PAID)) {
                    rr.setStatus(RentedRoomStatus.CANCELLED);
                    rentedRoomRepository.save(rr);
                    Set<User> tenants = new HashSet<>(rr.getCoTenants());
                    tenants.add(rr.getUser());
                    tenants.forEach(tenant -> {
                        CreateNotificationRequest notificationRequest = CreateNotificationRequest.builder()
                                .header("Phòng đã bị hủy do chưa thanh toán tiền cọc")
                                .body("Phòng "+ rr.getRoom().getId() + " đã bị hủy do chưa thanh toán tiền cọc.")
                                .userId(tenant.getId())
                                .extra(rr.getId())
                                .build();
                        notificationService.sendNotification(notificationRequest);
                    });
                }
            };
            taskScheduler.schedule(task, Instant.now().plus(AppConstants.DEPOSIT_PAYMENT_TIMEOUT,
                                                            TimeUnit.MINUTES.toChronoUnit()));
        } catch (Exception e) {
            log.error("Error processing deposit pay event for rented room ID: {}", rentedRoomId, e);
        }
    }

    private String sendSystemMessage(String rentedRoomId, ChatRoom chatRoom, String requesterId){
        ChatMessage chatMessage = ChatMessage.builder()
                .message("Yêu cầu thuê phòng đã được chấp nhận, vui lòng thanh toán tiền cọc trong 12 giờ, " +
                                 "để hoàn tất quá trình thuê phòng, chậm chân là mất ngay nhé")
                .chatRoom(chatRoom)
                .subId(chatRoom.getNextSubId() + 1)
                .build();
        String savedChatMessageId = chatMessageService.saveSystemMessage(chatMessage, chatRoom);
        chatRoom.setStatus(ChatRoomStatus.COMPLETED);
        chatRoom.setLastMessage(chatMessage.getMessage());
        chatRoom.setLastMessageTimeStamp(LocalDateTime.now());
        chatRoom.setNextSubId(chatRoom.getNextSubId() + 1);
        chatRoom.setRentedRoomId(rentedRoomId);
        chatRoomService.saveChatRoom(chatRoom);
        simpMessagingTemplate.convertAndSendToUser(requesterId, "/queue/chat-room",
                                                   chatRoom.getId());
        return savedChatMessageId;
    }
}
