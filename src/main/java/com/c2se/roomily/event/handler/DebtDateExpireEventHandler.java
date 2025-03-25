package com.c2se.roomily.event.handler;

import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.RentedRoomStatus;
import com.c2se.roomily.event.DebtDateExpireEvent;
import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.repository.RentedRoomRepository;
import com.c2se.roomily.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DebtDateExpireEventHandler {
    private final NotificationService notificationService;
    private final RentedRoomRepository rentedRoomRepository;

    @EventListener
    @Async
    public void handleDebtDateExpireEvent(DebtDateExpireEvent event) {
        log.info("Handling debt date expire event");
        RentedRoom rentedRoom = rentedRoomRepository.findById(event.getRentedRoomId())
                .orElseThrow(() -> new RuntimeException("Rented room not found"));
        if (rentedRoom.getStatus() != RentedRoomStatus.DEBT && rentedRoom.getStatus() != RentedRoomStatus.BILL_MISSING) {
            return;
        }
        Set<User> tenants = rentedRoom.getCoTenants();
        tenants.add(rentedRoom.getUser());
        // If the wallet debt is less than or equal to the rental deposit, pay the debt
        if (rentedRoom.getWalletDebt().compareTo(rentedRoom.getRentalDeposit()) <= 0) {
            rentedRoom.setWalletDebt(BigDecimal.ZERO);
            rentedRoom.setRentalDeposit(rentedRoom.getRentalDeposit().subtract(rentedRoom.getWalletDebt()));
            // If the rented room is in debt status, change it to in use status, send notification to tenants and landlord
            if (rentedRoom.getStatus() == RentedRoomStatus.DEBT) {
                rentedRoom.setStatus(RentedRoomStatus.IN_USE);
                sendPayFullDebtNotificationToTenants(tenants, rentedRoom);
            }
            // If the rented room is in bill missing status, send notification to tenants and landlord
            else {
                rentedRoom.setDebtDate(rentedRoom.getDebtDate().plusDays(3));
                sendPayFullBillMissingNotificationToTenants(tenants, rentedRoom);
            }
            rentedRoomRepository.save(rentedRoom);
            return;
        }
        // If the wallet debt is greater than the rental deposit, subtract the rental deposit from the wallet debt
        rentedRoom.setWalletDebt(rentedRoom.getWalletDebt().subtract(rentedRoom.getRentalDeposit()));
        rentedRoom.setRentalDeposit(BigDecimal.ZERO);
        // If the rented room is in debt status, send notification to tenants and landlord
        if (rentedRoom.getStatus() == RentedRoomStatus.DEBT) {
            sendPayNotFullDebtNotificationToTenants(tenants, rentedRoom);
        }
        // If the rented room is in bill missing status, send notification to tenants and landlord
        else {
            sendPayNotFullBillMissingNotificationToTenants(tenants, rentedRoom);
        }
        rentedRoomRepository.save(rentedRoom);
    }

    private void sendPayFullBillMissingNotificationToTenants(Set<User> tenants, RentedRoom rentedRoom) {
        tenants.forEach(tenant -> {
            CreateNotificationRequest tenantNotification = CreateNotificationRequest.builder()
                    .header("Quá hạn trả nợ tiền phòng")
                    .body("Đã tự động trừ tiền cọc để trả nợ tiền phòng, vẫn chưa cập nhật hóa đơn điện nước" +
                                  ", bạn có 3 ngày để cập nhật hóa đơn và thanh toán nợ.")
                    .userId(tenant.getId())
                    .type("DEBT_EXPIRED")
                    .extra(rentedRoom.getRoom().getId())
                    .build();
            notificationService.sendNotification(tenantNotification);
        });
        CreateNotificationRequest landlordNotification = CreateNotificationRequest.builder()
                .header("Đã tự động trừ tiền cọc để trả nợ tiền phòng")
                .body("Đã tự động trừ tiền cọc để trả nợ tiền phòng của phòng " + rentedRoom.getRoom().getId()
                              + ", vẫn chưa cập nhật hóa đơn điện nước.")
                .userId(rentedRoom.getRoom().getLandlord().getId())
                .type("DEBT_EXPIRED")
                .extra(rentedRoom.getRoom().getId())
                .build();
        notificationService.sendNotification(landlordNotification);
    }

    private void sendPayNotFullBillMissingNotificationToTenants(Set<User> tenants, RentedRoom rentedRoom) {
        CreateNotificationRequest tenantNotification = CreateNotificationRequest.builder()
                .header("Quá hạn trả nợ tiền phòng")
                .body("Đã tự động trừ tiền cọc để trả nợ tiền phòng nhưng không đủ tiền cọc để trả nợ," +
                              " vẫn chưa cập nhật hóa đơn điện nước.")
                .type("DEBT_EXPIRED")
                .extra(rentedRoom.getRoom().getId())
                .build();
        tenants.forEach(tenant -> {
            tenantNotification.setUserId(tenant.getId());
            notificationService.sendNotification(tenantNotification);
        });
        CreateNotificationRequest landlordNotification = CreateNotificationRequest.builder()
                .header("Đã tự động trừ tiền cọc để trả nợ tiền phòng")
                .body("Đã tự động trừ tiền cọc để trả nợ tiền phòng nhưng không đủ tiền cọc để trả nợ, " +
                              "còn lại " + rentedRoom.getWalletDebt() + " nợ, vẫn chưa cập nhật hóa đơn điện nước.")
                .userId(rentedRoom.getRoom().getLandlord().getId())
                .type("DEBT_EXPIRED")
                .extra(rentedRoom.getRoom().getId())
                .build();
        notificationService.sendNotification(landlordNotification);
    }

    private void sendPayFullDebtNotificationToTenants(Set<User> tenants, RentedRoom rentedRoom) {
        tenants.forEach(tenant -> {
            CreateNotificationRequest tenantNotification = CreateNotificationRequest.builder()
                    .header("Quá hạn trả nợ tiền phòng")
                    .body("Đã tự động trừ tiền cọc để trả nợ tiền phòng.")
                    .userId(tenant.getId())
                    .type("DEBT_EXPIRED")
                    .extra(rentedRoom.getRoom().getId())
                    .build();
            notificationService.sendNotification(tenantNotification);
        });
        CreateNotificationRequest landlordNotification = CreateNotificationRequest.builder()
                .header("Đã tự động trừ tiền cọc để trả nợ tiền phòng")
                .body("Đã tự động trừ tiền cọc để trả nợ tiền phòng của phòng " + rentedRoom.getRoom().getId())
                .userId(rentedRoom.getRoom().getLandlord().getId())
                .type("DEBT_EXPIRED")
                .extra(rentedRoom.getRoom().getId())
                .build();
        notificationService.sendNotification(landlordNotification);
    }

    private void sendPayNotFullDebtNotificationToTenants(Set<User> tenants, RentedRoom rentedRoom) {
        CreateNotificationRequest tenantNotification = CreateNotificationRequest.builder()
                .header("Quá hạn trả nợ tiền phòng")
                .body("Đã tự động trừ tiền cọc để trả nợ tiền phòng nhưng không đủ tiền cọc để trả nợ.")
                .type("DEBT_EXPIRED")
                .extra(rentedRoom.getRoom().getId())
                .build();
        tenants.forEach(tenant -> {
            tenantNotification.setUserId(tenant.getId());
            notificationService.sendNotification(tenantNotification);
        });
        CreateNotificationRequest landlordNotification = CreateNotificationRequest.builder()
                .header("Đã tự động trừ tiền cọc để trả nợ tiền phòng")
                .body("Đã tự động trừ tiền cọc để trả nợ tiền phòng nhưng không đủ tiền cọc để trả nợ, còn lại " + rentedRoom.getWalletDebt() + " để trả nợ.")
                .userId(rentedRoom.getRoom().getLandlord().getId())
                .type("DEBT_EXPIRED")
                .extra(rentedRoom.getRoom().getId())
                .build();
        notificationService.sendNotification(landlordNotification);
    }
}
