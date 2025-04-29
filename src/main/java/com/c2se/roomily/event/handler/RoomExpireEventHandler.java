package com.c2se.roomily.event.handler;

import com.c2se.roomily.entity.BillLog;
import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.RentedRoomStatus;
import com.c2se.roomily.event.pojo.RoomExpireEvent;
import com.c2se.roomily.payload.request.CreateBillLogRequest;
import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.payload.request.CreateRentedRoomActivityRequest;
import com.c2se.roomily.repository.RentedRoomRepository;
import com.c2se.roomily.service.BillLogService;
import com.c2se.roomily.service.NotificationService;
import com.c2se.roomily.service.RentedRoomActivityService;
import com.c2se.roomily.util.AppConstants;
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
@Slf4j
@RequiredArgsConstructor
public class RoomExpireEventHandler {
    private final NotificationService notificationService;
    private final RentedRoomRepository rentedRoomRepository;
    private final BillLogService billLogService;
    private final RentedRoomActivityService rentedRoomActivityService;

    @EventListener
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleRoomExpireEvent(RoomExpireEvent event) {
        log.info("Handling room expire event for room {}", event.getRoomId());
        RentedRoom rentedRoom = rentedRoomRepository.findById(event.getRentedRoomId())
                .orElseThrow(() -> new RuntimeException("Rented room not found"));
        
        rentedRoom.setStatus(RentedRoomStatus.BILL_MISSING);
        
        // The debtDate will be used for administrative actions
        LocalDate debtDate = rentedRoom.getEndDate().plusDays(AppConstants.DEBT_DATE_THRESHOLD);
        LocalDate lateDate = rentedRoom.getEndDate().plusDays(AppConstants.LATE_DATE_THRESHOLD);
        rentedRoom.setDebtDate(debtDate);
        
        // Handle rental cost
        BigDecimal rentalCost = rentedRoom.getRoom().getPrice();
        boolean isRentalCostPaid = false;
        if (rentedRoom.getRentedRoomWallet().compareTo(rentalCost) < 0) {
            // Not enough money for rental cost
            rentedRoom.setWalletDebt(rentalCost);
        } else {
            // Has enough for rental cost
            rentedRoom.setRentedRoomWallet(rentedRoom.getRentedRoomWallet().subtract(rentalCost));
            rentedRoom.setWalletDebt(BigDecimal.ZERO);
            isRentalCostPaid = true;
        }
        
        // Update dates for next period
        rentedRoom.setStartDate(rentedRoom.getEndDate().plusDays(1));
        rentedRoom.setEndDate(rentedRoom.getEndDate().plusMonths(1));
        rentedRoomRepository.save(rentedRoom);
        
        // Create new bill log with lateDate set
        CreateBillLogRequest billLogRequest = CreateBillLogRequest.builder()
                .fromDate(rentedRoom.getStartDate())
                .toDate(rentedRoom.getEndDate())
                .rentedRoomId(rentedRoom.getId())
                .rentalCost(rentalCost)
                .lateDate(lateDate)
                .isRentalCostPaid(isRentalCostPaid)
                .build();
        billLogService.createBillLog(billLogRequest);
        
        // Create activity and notifications
        CreateRentedRoomActivityRequest activityRequest = CreateRentedRoomActivityRequest.builder()
                .rentedRoomId(rentedRoom.getId())
                .message("Đã tới hạn thanh toán tiền phòng tháng này. Vui lòng cập nhật hóa đơn điện nước và thanh toán")
                .build();
        rentedRoomActivityService.createRentedRoomActivity(activityRequest);
        
        // Notify landlord
        CreateNotificationRequest landlordNotification = CreateNotificationRequest.builder()
                .header("Phòng đã đến hạn thanh toán")
                .body("Phòng " + rentedRoom.getRoom().getId() + " đã đến hạn thanh toán. Vui lòng kiểm tra hóa đơn.")
                .userId(rentedRoom.getLandlord().getId())
                .extra(rentedRoom.getRoom().getId())
                .build();
        notificationService.sendNotification(landlordNotification);
        
        // Notify tenants
        Set<User> tenants = new HashSet<>(rentedRoom.getCoTenants());
        tenants.add(rentedRoom.getUser());
        tenants.forEach(tenant -> {
            CreateNotificationRequest tenantNotification = CreateNotificationRequest.builder()
                    .header("Thanh toán tiền phòng")
                    .body("Phòng bạn đã tới hạn thanh toán. Vui lòng cập nhật hóa đơn và thanh toán")
                    .userId(tenant.getId())
                    .extra(rentedRoom.getRoom().getId())
                    .build();
            notificationService.sendNotification(tenantNotification);
        });
    }
}
