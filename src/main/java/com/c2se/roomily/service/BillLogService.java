package com.c2se.roomily.service;

import com.c2se.roomily.entity.BillLog;
import com.c2se.roomily.payload.request.CheckBillLogRequest;
import com.c2se.roomily.payload.request.CreateBillLogRequest;
import com.c2se.roomily.payload.request.UpdateBillLogRequest;
import com.c2se.roomily.payload.response.BillLogResponse;

import java.util.List;

public interface BillLogService {
    void save(BillLog billLog);

    BillLog getBillLogEntityById(String billLogId);

    BillLogResponse getBillLogById(String billLogId);

    List<BillLogResponse> getBillLogsByRoomId(String roomId);

    List<BillLogResponse> getBillLogsByRentedRoomId(String rentedRoomId);

    BillLogResponse getActiveBillLogByRoomId(String roomId);

    BillLog getActiveBillLogByRentedRoomId(String rentedRoomId);

    BillLogResponse getActiveBillLogResponseByRentedRoomId(String rentedRoomId);

    void checkBillLog(String billLogId, CheckBillLogRequest checkBillLogRequest);

    void processBillLog(BillLog billLogId);

    void createBillLog(CreateBillLogRequest createBillLogRequest);

    void updateBillLog(String billLogId, UpdateBillLogRequest updateBillLogRequest);
}
