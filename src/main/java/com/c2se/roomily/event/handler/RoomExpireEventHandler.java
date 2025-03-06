package com.c2se.roomily.event.handler;

import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.enums.RentedRoomStatus;
import com.c2se.roomily.event.RoomExpireEvent;
import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.repository.BillLogRepository;
import com.c2se.roomily.repository.RentedRoomRepository;
import com.c2se.roomily.repository.RoomRepository;
import com.c2se.roomily.service.NotificationService;
import com.c2se.roomily.util.AppConstants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@AllArgsConstructor
public class RoomExpireEventHandler {
    NotificationService notificationService;
    RoomRepository roomRepository;
    RentedRoomRepository rentedRoomRepository;
    BillLogRepository billLogRepository;

    @EventListener
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleRoomExpireEvent(RoomExpireEvent event) {
        log.info("Handling room expire event for room {}", event.getRoomId());
        RentedRoom rentedRoom = rentedRoomRepository.findActiveByRoomId(event.getRentedRoomId());
        rentedRoom.setStatus(RentedRoomStatus.DEBT);
        rentedRoom.setDebtDate(rentedRoom.getEndDate().plusDays(AppConstants.DEBT_DATE_THRESHOLD));
        rentedRoomRepository.save(rentedRoom);
        // TODO: Create bill log and handle
        CreateNotificationRequest userNotification = CreateNotificationRequest.builder()
                .header("Room Expired")
                .body("Your room has expired. Please update electric and water to pay rent.")
                .userId(rentedRoom.getUser().getId())
                .type("ROOM_EXPIRED")
                .build();
        CreateNotificationRequest landlordNotification = CreateNotificationRequest.builder()
                .header("Room Expired")
                .body("Your room has expired. Please collect the rent.")
                .userId(rentedRoom.getLandlord().getId())
                .type("ROOM_EXPIRED")
                .extra(rentedRoom.getRoom().getId())
                .build();
        notificationService.sendNotification(userNotification);
        notificationService.sendNotification(landlordNotification);
    }
}
