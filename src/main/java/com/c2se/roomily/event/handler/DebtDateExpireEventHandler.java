package com.c2se.roomily.event.handler;

import com.c2se.roomily.entity.BillLog;
import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.BillStatus;
import com.c2se.roomily.enums.RentedRoomStatus;
import com.c2se.roomily.event.pojo.DebtDateExpireEvent;
import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.payload.request.CreateRentedRoomActivityRequest;
import com.c2se.roomily.repository.RentedRoomRepository;
import com.c2se.roomily.service.NotificationService;
import com.c2se.roomily.service.impl.BillLogServiceImpl;
import com.c2se.roomily.service.impl.RentedRoomActivityServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DebtDateExpireEventHandler {
    private final NotificationService notificationService;
    private final RentedRoomRepository rentedRoomRepository;
    private final BillLogServiceImpl billLogService;
    private final RentedRoomActivityServiceImpl rentedRoomActivityService;

    @EventListener
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleDebtDateExpireEvent(DebtDateExpireEvent event) {
        log.info("Handling debt date expire event");
        RentedRoom rentedRoom = rentedRoomRepository.findById(event.getRentedRoomId())
                .orElseThrow(() -> new RuntimeException("Rented room not found"));
        
        if (rentedRoom.getStatus() != RentedRoomStatus.DEBT && rentedRoom.getStatus() != RentedRoomStatus.BILL_MISSING) {
            return;
        }

        BillLog activeBillLog = billLogService.getActiveBillLogByRentedRoomId(rentedRoom.getId());
        Set<User> tenants = new HashSet<>(rentedRoom.getCoTenants());
        tenants.add(rentedRoom.getUser());
        
        // If the wallet debt is less than or equal to the rental deposit, pay the debt from deposit
        if (rentedRoom.getWalletDebt().compareTo(rentedRoom.getRentalDeposit()) <= 0) {
            BigDecimal deductedAmount = rentedRoom.getWalletDebt();
            rentedRoom.setRentalDeposit(rentedRoom.getRentalDeposit().subtract(deductedAmount));
            rentedRoom.setWalletDebt(BigDecimal.ZERO);
            
            // If the rented room is in debt status, change it to IN_USE
            if (rentedRoom.getStatus() == RentedRoomStatus.DEBT) {
                rentedRoom.setStatus(RentedRoomStatus.IN_USE);
                
                // Update bill status to LATE_PAID since it's paid from deposit after late date
                if (activeBillLog != null) {
                    if (activeBillLog.getBillStatus() == BillStatus.UNPAID || 
                        activeBillLog.getBillStatus() == BillStatus.PENDING) {
                        // Check if it's late
                        if (activeBillLog.getLateDate() != null && 
                            LocalDate.now().isAfter(activeBillLog.getLateDate())) {
                            activeBillLog.setBillStatus(BillStatus.LATE_PAID);
                        } else {
                            activeBillLog.setBillStatus(BillStatus.PAID);
                        }
                    } else if (activeBillLog.getBillStatus() == BillStatus.LATE) {
                        activeBillLog.setBillStatus(BillStatus.LATE_PAID);
                    }
                    billLogService.save(activeBillLog);
                }
                
                sendPayFullDebtNotificationToTenants(tenants, rentedRoom);
                
                // Create activity log about deposit being used
                String paymentMessage = activeBillLog != null && activeBillLog.getBillStatus() == BillStatus.LATE_PAID
                    ? "Tiền cọc đã được sử dụng để thanh toán muộn hóa đơn: "
                    : "Tiền cọc đã được sử dụng để thanh toán hóa đơn: ";
                    
                CreateRentedRoomActivityRequest activity = CreateRentedRoomActivityRequest.builder()
                        .rentedRoomId(rentedRoom.getId())
                        .message(paymentMessage + deductedAmount + " VND.")
                        .build();
                rentedRoomActivityService.createRentedRoomActivity(activity);
            }
            // If bill missing, extend deadline
            else {
                rentedRoom.setDebtDate(rentedRoom.getDebtDate().plusDays(3));
                sendPayFullBillMissingNotificationToTenants(tenants, rentedRoom);
            }
            rentedRoomRepository.save(rentedRoom);
            return;
        }
        
        // If the wallet debt is greater than the rental deposit, subtract the rental deposit from the wallet debt
        BigDecimal deductedAmount = rentedRoom.getRentalDeposit();
        rentedRoom.setWalletDebt(rentedRoom.getWalletDebt().subtract(deductedAmount));
        rentedRoom.setRentalDeposit(BigDecimal.ZERO);
        
        // If the rented room is in debt status, send notification but bill remains LATE
        if (rentedRoom.getStatus() == RentedRoomStatus.DEBT) {
            // Bill remains as LATE because it's still not fully paid
            if (activeBillLog != null && 
                (activeBillLog.getBillStatus() == BillStatus.UNPAID || 
                 activeBillLog.getBillStatus() == BillStatus.PENDING)) {
                activeBillLog.setBillStatus(BillStatus.LATE);
                billLogService.save(activeBillLog);
            }
            
            sendPayNotFullDebtNotificationToTenants(tenants, rentedRoom);
            
            // Create activity log about deposit being used partially
            CreateRentedRoomActivityRequest activity = CreateRentedRoomActivityRequest.builder()
                    .rentedRoomId(rentedRoom.getId())
                    .message("Tiền cọc đã được sử dụng để thanh toán một phần hóa đơn: " + 
                            deductedAmount + " VND. Còn nợ: " + rentedRoom.getWalletDebt() + " VND.")
                    .build();
            rentedRoomActivityService.createRentedRoomActivity(activity);
        }
        // If bill missing, send notification
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
                    .extra(rentedRoom.getRoom().getId())
                    .build();
            notificationService.sendNotification(tenantNotification);
        });
        CreateNotificationRequest landlordNotification = CreateNotificationRequest.builder()
                .header("Đã tự động trừ tiền cọc để trả nợ tiền phòng")
                .body("Đã tự động trừ tiền cọc để trả nợ tiền phòng của phòng " + rentedRoom.getRoom().getId()
                              + ", vẫn chưa cập nhật hóa đơn điện nước.")
                .userId(rentedRoom.getRoom().getLandlord().getId())
                .extra(rentedRoom.getRoom().getId())
                .build();
        notificationService.sendNotification(landlordNotification);
    }

    private void sendPayNotFullBillMissingNotificationToTenants(Set<User> tenants, RentedRoom rentedRoom) {
        CreateNotificationRequest tenantNotification = CreateNotificationRequest.builder()
                .header("Quá hạn trả nợ tiền phòng")
                .body("Đã tự động trừ tiền cọc để trả nợ tiền phòng nhưng không đủ tiền cọc để trả nợ," +
                              " vẫn chưa cập nhật hóa đơn điện nước.")
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
                    .extra(rentedRoom.getRoom().getId())
                    .build();
            notificationService.sendNotification(tenantNotification);
        });
        CreateNotificationRequest landlordNotification = CreateNotificationRequest.builder()
                .header("Đã tự động trừ tiền cọc để trả nợ tiền phòng")
                .body("Đã tự động trừ tiền cọc để trả nợ tiền phòng của phòng " + rentedRoom.getRoom().getId())
                .userId(rentedRoom.getRoom().getLandlord().getId())
                .extra(rentedRoom.getRoom().getId())
                .build();
        notificationService.sendNotification(landlordNotification);
    }

    private void sendPayNotFullDebtNotificationToTenants(Set<User> tenants, RentedRoom rentedRoom) {
        CreateNotificationRequest tenantNotification = CreateNotificationRequest.builder()
                .header("Quá hạn trả nợ tiền phòng")
                .body("Đã tự động trừ tiền cọc để trả nợ tiền phòng nhưng không đủ tiền cọc để trả nợ.")
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
                .extra(rentedRoom.getRoom().getId())
                .build();
        notificationService.sendNotification(landlordNotification);
    }
}
