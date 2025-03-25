package com.c2se.roomily.service.impl;

import com.c2se.roomily.config.StorageConfig;
import com.c2se.roomily.entity.BillLog;
import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.enums.BillStatus;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.enums.RentedRoomStatus;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.CheckBillLogRequest;
import com.c2se.roomily.payload.request.CreateBillLogRequest;
import com.c2se.roomily.payload.request.UpdateBillLogRequest;
import com.c2se.roomily.payload.response.BillLogResponse;
import com.c2se.roomily.repository.BillLogRepository;
import com.c2se.roomily.service.BillLogService;
import com.c2se.roomily.service.RentedRoomService;
import com.c2se.roomily.service.RoomService;
import com.c2se.roomily.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillLogServiceImpl implements BillLogService {
    private final BillLogRepository billLogRepository;
    private final RentedRoomService rentedRoomService;
    private final RoomService roomService;
    private final StorageService storageService;
    private final StorageConfig storageConfig;

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
    public List<BillLogResponse> getActiveBillLogByRoomId(String roomId) {
        return billLogRepository.findActiveBillLogByRoomId(roomId)
                .stream().map(this::mapBillLogToBillLogResponse).collect(Collectors.toList());
    }

    @Override
    public List<BillLogResponse> getActiveBillLogByRentedRoomId(String rentedRoomId) {
        return billLogRepository.findActiveBillLogByRentedRoomId(rentedRoomId)
                .stream().map(this::mapBillLogToBillLogResponse).collect(Collectors.toList());
    }

    @Override
    public void createBillLog(CreateBillLogRequest createBillLogRequest) {
        BillLog billLog = BillLog.builder()
                .fromDate(createBillLogRequest.getFromDate())
                .toDate(createBillLogRequest.getToDate())
                .rentalCost(createBillLogRequest.getRentalCost())
                .rentedRoom(rentedRoomService.getRentedRoomEntityById(createBillLogRequest.getRentedRoomId()))
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
        processBillLog(billLogId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processBillLog(String billLogId) {
        BillLog billLog = getBillLogEntityById(billLogId);
        RentedRoom rentedRoom = billLog.getRentedRoom();
        if (billLog.getBillStatus().equals(BillStatus.PENDING)) {
            BigDecimal waterPrice = rentedRoom.getRoom().getWaterPrice();
            BigDecimal electricityPrice = rentedRoom.getRoom().getElectricityPrice();
            BigDecimal waterBill = waterPrice.multiply(BigDecimal.valueOf(billLog.getWaterAmount()));
            BigDecimal electricityBill = electricityPrice.multiply(BigDecimal.valueOf(billLog.getElectricityAmount()));
            billLog.setWaterCost(waterBill);
            billLog.setElectricityCost(electricityBill);
            BigDecimal totalBill = waterBill.add(electricityBill).add(rentedRoom.getWalletDebt());
            // Check if the rented room has enough money to pay the bill
            if (rentedRoom.getRentedRoomWallet().compareTo(totalBill) >= 0){
                rentedRoom.setRentedRoomWallet(rentedRoom.getRentedRoomWallet().subtract(totalBill));
                rentedRoom.setDebtDate(null);
                rentedRoom.setStatus(RentedRoomStatus.IN_USE);
                rentedRoom.setWalletDebt(BigDecimal.ZERO);
                billLog.setBillStatus(BillStatus.PAID);
            } else {
                rentedRoom.setStatus(RentedRoomStatus.DEBT);
                rentedRoom.setWalletDebt(totalBill);
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
            if (updateBillLogRequest.getWaterImage() != null)
                storageService.putObject(updateBillLogRequest.getWaterImage(),
                                     storageConfig.getBucketStore(),
                                     generateBillLogImageUrl("water", billLogId));
            if (updateBillLogRequest.getElectricityImage() != null)
                storageService.putObject(updateBillLogRequest.getElectricityImage(),
                                     storageConfig.getBucketStore(),
                                     generateBillLogImageUrl("electricity", billLogId));
        } catch (Exception e){
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR,
                                   "Error while uploading image: " + e.getMessage());
        }
        billLog.setBillStatus(BillStatus.CHECKING);
        billLogRepository.save(billLog);
    }

    private BillLogResponse mapBillLogToBillLogResponse(BillLog billLog) {
        return BillLogResponse.builder()
                .id(billLog.getId())
                .fromDate(billLog.getFromDate())
                .toDate(billLog.getToDate())
                .electricity(billLog.getElectricityAmount())
                .water(billLog.getWaterAmount())
                .electricityBill(billLog.getElectricityCost())
                .waterBill(billLog.getWaterCost())
                .electricityImageUrl(billLog.getElectricityImage())
                .waterImageUrl(billLog.getWaterImage())
                .rentalCost(billLog.getRentalCost())
                .billStatus(billLog.getBillStatus().name())
                .createdAt(billLog.getCreatedAt())
                .roomId(billLog.getRoomId())
                .rentedRoomId(billLog.getRentedRoom().getId())
                .build();
    }

    private String generateBillLogImageUrl(String billLogId, String type) {
        return String.format("%s-%s-%s", billLogId, type, UUID.randomUUID());
    }
}
