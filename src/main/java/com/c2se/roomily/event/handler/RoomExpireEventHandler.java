package com.c2se.roomily.event.handler;

import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.RentedRoomActivityType;
import com.c2se.roomily.enums.RentedRoomStatus;
import com.c2se.roomily.event.RoomExpireEvent;
import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.payload.request.CreateRentedRoomActivityRequest;
import com.c2se.roomily.repository.BillLogRepository;
import com.c2se.roomily.repository.RentedRoomRepository;
import com.c2se.roomily.repository.RoomRepository;
import com.c2se.roomily.service.NotificationService;
import com.c2se.roomily.service.RentedRoomActivityService;
import com.c2se.roomily.util.AppConstants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
@AllArgsConstructor
public class RoomExpireEventHandler {
    NotificationService notificationService;
    RoomRepository roomRepository;
    RentedRoomRepository rentedRoomRepository;
    BillLogRepository billLogRepository;
    RentedRoomActivityService rentedRoomActivityService;

    @EventListener
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleRoomExpireEvent(RoomExpireEvent event) {
        log.info("Handling room expire event for room {}", event.getRoomId());
        RentedRoom rentedRoom = rentedRoomRepository.findActiveByRoomId(event.getRentedRoomId(),
                                                                        List.of(RentedRoomStatus.IN_USE,
                                                                                RentedRoomStatus.DEBT));
        rentedRoom.setStatus(RentedRoomStatus.DEBT);
        rentedRoom.setDebtDate(rentedRoom.getEndDate().plusDays(AppConstants.DEBT_DATE_THRESHOLD));
        rentedRoom.setRentedRoomWallet(rentedRoom.getRentedRoomWallet().subtract(rentedRoom.getRoom().getPrice()));
        rentedRoomRepository.save(rentedRoom);
        CreateRentedRoomActivityRequest activityRequest = CreateRentedRoomActivityRequest.builder()
                .rentedRoomId(rentedRoom.getId())
                .activityType(RentedRoomActivityType.DEBT_RECORDED.name())
                .message(
                        "Đã tới hạn thanh toán tiền phòng tháng này. Vui lòng cập nhật hóa đơn điện nước và thanh toán")
                .build();
        rentedRoomActivityService.createRentedRoomActivity(activityRequest);
        // TODO: Create bill log and handle
        CreateNotificationRequest landlordNotification = CreateNotificationRequest.builder()
                .header("Room Expired")
                .body("Your room has expired. Please collect the rent.")
                .userId(rentedRoom.getLandlord().getId())
                .type("ROOM_EXPIRED")
                .extra(rentedRoom.getRoom().getId())
                .build();
        notificationService.sendNotification(landlordNotification);
        Set<User> tenants = rentedRoom.getCoTenants();
        tenants.add(rentedRoom.getUser());
        tenants.forEach(tenant -> {
            CreateNotificationRequest tenantNotification = CreateNotificationRequest.builder()
                    .header("Thanh toán tiền phòng")
                    .body("Phòng bạn đã tới hạn thanh toán. Vui lòng cập nhật hóa đơn và thanh toán")
                    .userId(tenant.getId())
                    .type("ROOM_EXPIRED")
                    .extra(rentedRoom.getRoom().getId())
                    .build();
            notificationService.sendNotification(tenantNotification);
        });
    }
}
