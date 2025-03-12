package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.CheckBillLogRequest;
import com.c2se.roomily.payload.request.UpdateBillLogRequest;
import com.c2se.roomily.payload.response.BillLogResponse;
import com.c2se.roomily.service.BillLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bill-logs")
@RequiredArgsConstructor
public class BillLogController {
    private final BillLogService billLogService;
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<BillLogResponse>> getBillLogsByRoomId(@PathVariable String roomId) {
        return ResponseEntity.ok(billLogService.getBillLogsByRoomId(roomId));
    }

    @GetMapping("/{billLogId}")
    public ResponseEntity<BillLogResponse> getBillLogById(@PathVariable String billLogId) {
        return ResponseEntity.ok(billLogService.getBillLogById(billLogId));
    }

    @GetMapping("/rented-room/{rentedRoomId}")
    public ResponseEntity<List<BillLogResponse>> getBillLogsByRentedRoomId(@PathVariable String rentedRoomId) {
        return ResponseEntity.ok(billLogService.getBillLogsByRentedRoomId(rentedRoomId));
    }

    @GetMapping("/active/room/{roomId}")
    public ResponseEntity<List<BillLogResponse>> getActiveBillLogByRoomId(@PathVariable String roomId) {
        return ResponseEntity.ok(billLogService.getActiveBillLogByRoomId(roomId));
    }

    @GetMapping("/active/rented-room/{rentedRoomId}")
    public ResponseEntity<List<BillLogResponse>> getActiveBillLogByRentedRoomId(@PathVariable String rentedRoomId) {
        return ResponseEntity.ok(billLogService.getActiveBillLogByRentedRoomId(rentedRoomId));
    }

    @PostMapping("/{billLogId}/check")
    public ResponseEntity<Void> checkBillLog(@PathVariable String billLogId,
                                             @RequestBody CheckBillLogRequest checkBillLogRequest) {
        billLogService.checkBillLog(billLogId, checkBillLogRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{billLogId}/process")
    public ResponseEntity<Void> processBillLog(@PathVariable String billLogId) {
        billLogService.processBillLog(billLogId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{billLogId}")
    public ResponseEntity<Void> updateBillLog(@PathVariable String billLogId,
                                              @RequestBody UpdateBillLogRequest updateBillLogRequest) {
        billLogService.updateBillLog(billLogId, updateBillLogRequest);
        return ResponseEntity.ok().build();
    }
}
