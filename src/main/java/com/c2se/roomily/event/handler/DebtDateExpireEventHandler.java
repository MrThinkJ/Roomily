package com.c2se.roomily.event.handler;

import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.RentedRoomActivityType;
import com.c2se.roomily.enums.RentedRoomStatus;
import com.c2se.roomily.event.DebtDateExpireEvent;
import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.payload.request.CreateRentedRoomActivityRequest;
import com.c2se.roomily.repository.RentedRoomRepository;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.service.NotificationService;
import com.c2se.roomily.service.RentedRoomActivityService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@AllArgsConstructor
@Slf4j
public class DebtDateExpireEventHandler {
    NotificationService notificationService;
    UserRepository userRepository;
    RentedRoomRepository rentedRoomRepository;
    RentedRoomActivityService rentedRoomActivityService;

    @EventListener
    @Async
    public void handleDebtDateExpireEvent(DebtDateExpireEvent event) {
        log.info("Handling debt date expire event");
        RentedRoom rentedRoom = rentedRoomRepository.findActiveByRoomId(event.getRentedRoomId(),
                                                                        List.of(RentedRoomStatus.IN_USE,
                                                                                RentedRoomStatus.DEBT));
        CreateRentedRoomActivityRequest activityRequest = CreateRentedRoomActivityRequest.builder()
                .rentedRoomId(rentedRoom.getId())
                .activityType(RentedRoomActivityType.DEBT_RECORDED.name())
                .message("Đã quá hạn trả nợ. Vui lòng cập nhật hóa đơn và thanh toán")
                .build();
        rentedRoomActivityService.createRentedRoomActivity(activityRequest);
        CreateNotificationRequest landlordNotification = CreateNotificationRequest.builder()
                .header("Quá hạn trả nợ tiền phòng")
                .body("Phòng của bạn đã quá hạn trả nợ.")
                .userId(rentedRoom.getLandlord().getId())
                .type("DEBT_EXPIRED")
                .extra(rentedRoom.getRoom().getId())
                .build();
        notificationService.sendNotification(landlordNotification);
        Set<User> tenants = rentedRoom.getCoTenants();
        tenants.add(rentedRoom.getUser());
        tenants.forEach(tenant -> {
            CreateNotificationRequest tenantNotification = CreateNotificationRequest.builder()
                    .header("Quá hạn trả nợ tiền phòng")
                    .body("Phòng bạn đã quá hạn trả nợ. Đã thông báo cho chủ nhà. Vui lòng cập nhật hóa đơn và thanh toán")
                    .userId(tenant.getId())
                    .type("DEBT_EXPIRED")
                    .extra(rentedRoom.getRoom().getId())
                    .build();
            notificationService.sendNotification(tenantNotification);
        });
    }
}
