package com.c2se.roomily.event.handler;

import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.RentedRoomStatus;
import com.c2se.roomily.event.DepositPayEvent;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.repository.RentedRoomRepository;
import com.c2se.roomily.service.NotificationService;
import com.c2se.roomily.service.RentedRoomService;
import com.c2se.roomily.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositPayEventHandler {
    private final RentedRoomRepository rentedRoomRepository;
    private final NotificationService notificationService;
    private final TaskScheduler scheduler;
    @EventListener
    @Async
    @Transactional
    public void handleDepositPayEvent(DepositPayEvent event) {
        String taskId = "deposit-pay-" + event.getRentedRoomId();
        log.info("Processing deposit pay event for rented room ID: {}", event.getRentedRoomId());
        try {
            Runnable task = () -> {
                RentedRoom rentedRoom = rentedRoomRepository.findById(event.getRentedRoomId()).orElseThrow(
                        () -> new ResourceNotFoundException("Rented room", "id", event.getRentedRoomId())
                );
                if (rentedRoom.getStatus().equals(RentedRoomStatus.DEPOSIT_NOT_PAID)) {
                    rentedRoom.setStatus(RentedRoomStatus.CANCELLED);
                    rentedRoomRepository.save(rentedRoom);
                    Set<User> tenants = rentedRoom.getCoTenants();
                    tenants.add(rentedRoom.getUser());
                    tenants.forEach(tenant -> {
                        CreateNotificationRequest notificationRequest = CreateNotificationRequest.builder()
                                .header("Phòng đã bị hủy do chưa thanh toán tiền cọc")
                                .body("Phòng "+ rentedRoom.getRoom().getId() + " đã bị hủy do chưa thanh toán tiền cọc.")
                                .userId(tenant.getId())
                                .type("DEPOSIT_NOT_PAID")
                                .extra(rentedRoom.getId())
                                .build();
                        notificationService.sendNotification(notificationRequest);
                    });
                }
            };
            scheduler.schedule(task, Instant.now().plus(AppConstants.DEPOSIT_PAYMENT_TIMEOUT, TimeUnit.MINUTES.toChronoUnit()));
        } catch (Exception e) {
            log.error("Error processing deposit pay event for rented room ID: {}", event.getRentedRoomId(), e);
        }
    }
}
