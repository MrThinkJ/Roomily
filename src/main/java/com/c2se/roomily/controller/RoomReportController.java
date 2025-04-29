package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.CreateRoomReportRequest;
import com.c2se.roomily.payload.response.RoomReportResponse;
import com.c2se.roomily.service.RoomReportService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/room-reports")
@RequiredArgsConstructor
public class RoomReportController extends BaseController{
    private final RoomReportService roomReportService;

    @GetMapping("/{roomId}")
    public ResponseEntity<List<RoomReportResponse>> getAllRoomReports(@PathVariable String roomId,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(roomReportService.getAllRoomReports(roomId, page, size));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<RoomReportResponse>> getAllRoomReportsByStatus(@PathVariable String status,
                                                                                @RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(roomReportService.getAllRoomReportsByStatus(status, page, size));
    }

    @GetMapping("/room/{roomId}/status/{status}")
    public ResponseEntity<List<RoomReportResponse>> getAllRoomReportsByRoomIdAndStatus(@PathVariable String roomId,
                                                                                         @PathVariable String status,
                                                                                         @RequestParam(defaultValue = "0") int page,
                                                                                         @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(roomReportService.getAllRoomReportsByRoomIdAndStatus(roomId, status, page, size));
    }

    @GetMapping("/reporter/{reporterId}")
    public ResponseEntity<List<RoomReportResponse>> getAllRoomReportsByReporterId(@PathVariable String reporterId,
                                                                                     @RequestParam(defaultValue = "0") int page,
                                                                                     @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(roomReportService.getAllRoomReportsByReporterId(reporterId, page, size));
    }

    @PostMapping
    public ResponseEntity<Void> reportRoom(@RequestBody CreateRoomReportRequest createRoomReportRequest) {
        String reporterId = this.getUserInfo().getId();
        roomReportService.reportRoom(reporterId, createRoomReportRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/process/{reportId}/{isValid}")
    public ResponseEntity<Void> processReport(@PathVariable String reportId,
                                              @PathVariable Boolean isValid) {
        roomReportService.processReport(reportId, isValid);
        return ResponseEntity.ok().build();
    }
}
