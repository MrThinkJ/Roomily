package com.c2se.roomily.service.impl;

import com.c2se.roomily.config.StorageConfig;
import com.c2se.roomily.entity.BillLog;
import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.BillStatus;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.enums.RentedRoomStatus;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.*;
import com.c2se.roomily.payload.response.BillLogResponse;
import com.c2se.roomily.repository.BillLogRepository;
import com.c2se.roomily.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillLogServiceImpl implements BillLogService {
    private final BillLogRepository billLogRepository;
    private final RentedRoomService rentedRoomService;
    private final StorageService storageService;
    private final StorageConfig storageConfig;
    private final NotificationService notificationService;

    @Override
    public void save(BillLog billLog) {
        billLogRepository.save(billLog);
    }

    @Override
    public BillLog getBillLogEntityById(String billLogId) {
        return billLogRepository.findById(billLogId).orElseThrow(
                () -> new ResourceNotFoundException("BillLog", "id", billLogId)
        );
    }

    @Override
    public BillLogResponse getBillLogById(String billLogId) {
        return mapBillLogToBillLogResponse(getBillLogEntityById(billLogId));
    }

    @Override
    public List<BillLogResponse> getBillLogsByRoomId(String roomId) {
        return billLogRepository.findByRoomId(roomId)
                .stream().map(this::mapBillLogToBillLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BillLogResponse> getBillLogsByRentedRoomId(String rentedRoomId) {
        return billLogRepository.findByRentedRoomId(rentedRoomId)
                .stream().map(this::mapBillLogToBillLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BillLogResponse getActiveBillLogByRoomId(String roomId) {
        BillLog billLog = billLogRepository.findActiveBillLogByRoomId(roomId).orElse(null);
        if (billLog == null)
            return null;
        return mapBillLogToBillLogResponse(billLog);
    }

    @Override
    public BillLog getActiveBillLogByRentedRoomId(String rentedRoomId) {
        return billLogRepository.findActiveBillLogByRentedRoomId(rentedRoomId).orElse(null);
    }

    @Override
    public BillLogResponse getActiveBillLogResponseByRentedRoomId(String rentedRoomId) {
        BillLog billLog = getActiveBillLogByRentedRoomId(rentedRoomId);
        if (billLog == null)
            return null;
        return mapBillLogToBillLogResponse(billLog);
    }

    @Override
    public void createBillLog(CreateBillLogRequest createBillLogRequest) {
        RentedRoom rentedRoom = rentedRoomService.getRentedRoomEntityById(createBillLogRequest.getRentedRoomId());
        BillLog billLog = BillLog.builder()
                .fromDate(createBillLogRequest.getFromDate())
                .toDate(createBillLogRequest.getToDate())
                .rentalCost(createBillLogRequest.getRentalCost())
                .rentedRoom(rentedRoom)
                .roomId(rentedRoom.getRoom().getId())
                .billStatus(BillStatus.MISSING)
                .build();
        billLogRepository.save(billLog);
    }

    @Override
    public void checkBillLog(String billLogId, CheckBillLogRequest checkBillLogRequest) {
        BillLog billLog = getBillLogEntityById(billLogId);
        if (!checkBillLogRequest.getIsElectricityChecked() && !checkBillLogRequest.getIsWaterChecked()) {
            billLog.setBillStatus(BillStatus.RE_ENTER);
        } else if (!checkBillLogRequest.getIsWaterChecked()) {
            billLog.setBillStatus(BillStatus.WATER_RE_ENTER);
        } else if (!checkBillLogRequest.getIsElectricityChecked()) {
            billLog.setBillStatus(BillStatus.ELECTRICITY_RE_ENTER);
        } else {
            billLog.setBillStatus(BillStatus.PENDING);
        }
        billLog.setLandlordComment(checkBillLogRequest.getLandlordComment());
        billLogRepository.save(billLog);
        processBillLog(billLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processBillLog(BillLog billLog) {
        RentedRoom rentedRoom = billLog.getRentedRoom();
        if (billLog.getBillStatus().equals(BillStatus.PENDING)) {
            BigDecimal waterPrice = rentedRoom.getRoom().getWaterPrice();
            BigDecimal electricityPrice = rentedRoom.getRoom().getElectricityPrice();
            BigDecimal waterBill = waterPrice.multiply(BigDecimal.valueOf(billLog.getWaterAmount()));
            BigDecimal electricityBill = electricityPrice.multiply(BigDecimal.valueOf(billLog.getElectricityAmount()));
            billLog.setWaterCost(waterBill);
            billLog.setElectricityCost(electricityBill);
            BigDecimal totalBill = waterBill.add(electricityBill).add(rentedRoom.getWalletDebt());
            
            // Set lateDate to 3 days from now
            billLog.setLateDate(LocalDate.now().plusDays(3));
            
            // Check if the rented room has enough money to pay the bill
            if (rentedRoom.getRentedRoomWallet().compareTo(totalBill) >= 0) {
                rentedRoom.setRentedRoomWallet(rentedRoom.getRentedRoomWallet().subtract(totalBill));
                rentedRoom.setDebtDate(null);
                rentedRoom.setStatus(RentedRoomStatus.IN_USE);
                rentedRoom.setWalletDebt(BigDecimal.ZERO);
                billLog.setBillStatus(BillStatus.PAID);
            } else {
                // Set to UNPAID status
                rentedRoom.setStatus(RentedRoomStatus.DEBT);
                rentedRoom.setWalletDebt(totalBill);
                billLog.setBillStatus(BillStatus.UNPAID);
                
                // Set debt date if not already set
                if (rentedRoom.getDebtDate() == null) {
                    rentedRoom.setDebtDate(billLog.getLateDate());
                }
            }
            rentedRoomService.saveRentedRoom(rentedRoom);
            billLogRepository.save(billLog);
        }
    }

    @Override
    public void updateBillLog(String billLogId, UpdateBillLogRequest updateBillLogRequest) {
        BillLog billLog = getBillLogEntityById(billLogId);
        if (updateBillLogRequest.getElectricity() != null)
            billLog.setElectricityAmount(updateBillLogRequest.getElectricity());
        if (updateBillLogRequest.getWater() != null)
            billLog.setWaterAmount(updateBillLogRequest.getWater());
        try{
            if (updateBillLogRequest.getWaterImage() != null){
                String waterImageUrl = generateBillLogImageUrl(billLogId, "water", billLog.getRentedRoom().getId());
                storageService.putObject(updateBillLogRequest.getWaterImage(),
                                         storageConfig.getBucketStore(),
                                         generateBillLogImageUrl(billLogId, "water", billLog.getRentedRoom().getId()));
                billLog.setWaterImage(waterImageUrl);
            }
            if (updateBillLogRequest.getElectricityImage() != null){
                String electricityImageUrl = generateBillLogImageUrl(billLogId, "electricity", billLog.getRentedRoom().getId());
                storageService.putObject(updateBillLogRequest.getElectricityImage(),
                                         storageConfig.getBucketStore(),
                                         generateBillLogImageUrl(billLogId, "electricity", billLog.getRentedRoom().getId()));
                billLog.setElectricityImage(electricityImageUrl);
            }
        } catch (Exception e){
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR,
                                   "Error while uploading image: " + e.getMessage());
        }
        billLog.setBillStatus(BillStatus.CHECKING);
        billLogRepository.save(billLog);
    }

    private BillLogResponse mapBillLogToBillLogResponse(BillLog billLog) {
        String electricityImageUrl = null;
        String waterImageUrl = null;
        try {
            if (billLog.getElectricityImage() != null)
                electricityImageUrl = storageService.generatePresignedUrl(storageConfig.getBucketStore(),
                                                                         billLog.getElectricityImage());
            if (billLog.getWaterImage() != null)
                waterImageUrl = storageService.generatePresignedUrl(storageConfig.getBucketStore(),
                                                                   billLog.getWaterImage());
        } catch (Exception e){
            e.printStackTrace();
        }
        return BillLogResponse.builder()
                .id(billLog.getId())
                .fromDate(billLog.getFromDate())
                .toDate(billLog.getToDate())
                .electricity(billLog.getElectricityAmount())
                .water(billLog.getWaterAmount())
                .electricityBill(billLog.getElectricityCost())
                .waterBill(billLog.getWaterCost())
                .electricityImageUrl(electricityImageUrl)
                .waterImageUrl(waterImageUrl)
                .rentalCost(billLog.getRentalCost())
                .billStatus(billLog.getBillStatus().name())
                .createdAt(billLog.getCreatedAt())
                .roomId(billLog.getRoomId())
                .rentedRoomId(billLog.getRentedRoom().getId())
                .build();
    }

    private String generateBillLogImageUrl(String billLogId, String type, String rentedRoomId) {
        return String.format("%s-%s-%s", billLogId, type, rentedRoomId);
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional(rollbackFor = Exception.class)
    public void checkForLateBills() {
        // Find all UNPAID bills
        List<BillLog> unpaidBills = billLogRepository.findByBillStatus(BillStatus.UNPAID);
        
        LocalDate today = LocalDate.now();
        for (BillLog bill : unpaidBills) {
            RentedRoom rentedRoom = bill.getRentedRoom();
            
            // If approaching late date (2 days before), send reminder
            if (bill.getLateDate() != null && 
                bill.getLateDate().minusDays(2).equals(today)) {
                
                // Send notification to tenant reminding them of upcoming due date
                Set<User> tenants = new HashSet<>(rentedRoom.getCoTenants());
                tenants.add(rentedRoom.getUser());
                
                for (User tenant : tenants) {
                    CreateNotificationRequest reminderNotification = CreateNotificationRequest.builder()
                            .header("Nhắc nhở thanh toán hóa đơn")
                            .body("Hóa đơn của phòng " + rentedRoom.getRoom().getId() + 
                                  " sẽ đến hạn trong 2 ngày. Vui lòng thanh toán để tránh phí phạt.")
                            .userId(tenant.getId())
                            .extra(rentedRoom.getId())
                            .build();
                    notificationService.sendNotification(reminderNotification);
                }
            }
            
            // If past the late date, mark as LATE
            if (bill.getLateDate() != null && today.isAfter(bill.getLateDate())) {
                bill.setBillStatus(BillStatus.LATE);
                billLogRepository.save(bill);
                
                // Send notifications about bill being late
                Set<User> tenants = new HashSet<>(rentedRoom.getCoTenants());
                tenants.add(rentedRoom.getUser());
                
                for (User tenant : tenants) {
                    CreateNotificationRequest lateNotification = CreateNotificationRequest.builder()
                            .header("Hóa đơn quá hạn")
                            .body("Hóa đơn của phòng " + rentedRoom.getRoom().getId() + 
                                  " đã quá hạn thanh toán. Vui lòng thanh toán ngay để tránh bị phạt thêm.")
                            .userId(tenant.getId())
                            .extra(rentedRoom.getId())
                            .build();
                    notificationService.sendNotification(lateNotification);
                }
                
                // Notify landlord
                CreateNotificationRequest landlordNotification = CreateNotificationRequest.builder()
                        .header("Hóa đơn quá hạn")
                        .body("Hóa đơn của phòng " + rentedRoom.getRoom().getId() + " đã quá hạn thanh toán.")
                        .userId(rentedRoom.getLandlord().getId())
                        .extra(rentedRoom.getId())
                        .build();
                notificationService.sendNotification(landlordNotification);
            }
        }
    }
}
