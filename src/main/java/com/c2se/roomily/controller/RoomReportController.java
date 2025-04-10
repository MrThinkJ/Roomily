package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.CreateRoomReportRequest;
import com.c2se.roomily.service.RoomReportService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/room-reports")
@AllArgsConstructor
public class RoomReportController {
    RoomReportService roomReportService;

    @PostMapping
    public ResponseEntity<Void> reportRoom(String reporterId, @RequestBody CreateRoomReportRequest createRoomReportRequest) {
        roomReportService.reportRoom(reporterId, createRoomReportRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/process")
    public ResponseEntity<Void> processReport(String reportId, Boolean isValid) {
        roomReportService.processReport(reportId, isValid);
        return ResponseEntity.ok().build();
    }
}
