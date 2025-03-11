package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.BillLog;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.CreateBillLogRequest;
import com.c2se.roomily.payload.response.BillLogResponse;
import com.c2se.roomily.repository.BillLogRepository;
import com.c2se.roomily.service.BillLogService;
import com.c2se.roomily.service.RentedRoomService;
import com.c2se.roomily.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillLogServiceImpl implements BillLogService {
    private final BillLogRepository billLogRepository;
    private final RentedRoomService rentedRoomService;
    private final RoomService roomService;

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
    public void checkBillLog(String billLogId) {

    }

    @Override
    public void processBillLog(String billLogId) {

    }

    @Override
    public void createBillLog(CreateBillLogRequest createBillLogRequest) {

    }

    @Override
    public void updateBillLog(String billLogId, CreateBillLogRequest createBillLogRequest) {

    }

    private BillLogResponse mapBillLogToBillLogResponse(BillLog billLog) {
        return BillLogResponse.builder()
                .id(billLog.getId())
                .fromDate(billLog.getFromDate())
                .toDate(billLog.getToDate())
                .electricity(billLog.getElectricity())
                .water(billLog.getWater())
                .electricityBill(billLog.getElectricityBill())
                .waterBill(billLog.getWaterBill())
                .electricityImageUrl(billLog.getElectricityImage())
                .waterImageUrl(billLog.getWaterImage())
                .rentalCost(billLog.getRentalCost())
                .billStatus(billLog.getBillStatus().name())
                .createdAt(billLog.getCreatedAt())
                .roomId(billLog.getRoomId())
                .rentedRoomId(billLog.getRentedRoom().getId())
                .build();
    }
}
