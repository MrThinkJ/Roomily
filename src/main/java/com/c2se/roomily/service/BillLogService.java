package com.c2se.roomily.service;

import com.c2se.roomily.entity.BillLog;
import com.c2se.roomily.payload.request.CreateBillLogRequest;
import com.c2se.roomily.payload.response.BillLogResponse;

import java.util.List;

public interface BillLogService {
    BillLog getBillLogEntityById(String billLogId);

    BillLogResponse getBillLogById(String billLogId);

    List<BillLogResponse> getBillLogsByRoomId(String roomId);

    List<BillLogResponse> getBillLogsByRentedRoomId(String rentedRoomId);

    List<BillLogResponse> getActiveBillLogByRoomId(String roomId);

    List<BillLogResponse> getActiveBillLogByRentedRoomId(String rentedRoomId);

    void checkBillLog(String billLogId);

    void processBillLog(String billLogId);

    void createBillLog(CreateBillLogRequest createBillLogRequest);

    void updateBillLog(String billLogId, CreateBillLogRequest createBillLogRequest);
}
