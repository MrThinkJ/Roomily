package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.CreateRoomReportRequest;
import com.c2se.roomily.service.RoomReportService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/room-reports")
@AllArgsConstructor
public class RoomReportController {
    RoomReportService roomReportService;
    @PostMapping
    public void reportRoom(String reporterId, CreateRoomReportRequest createRoomReportRequest) {
        roomReportService.reportRoom(reporterId, createRoomReportRequest);
    }

    @PostMapping("/process")
    public void processReport(String reportId, Boolean isValid) {
        roomReportService.processReport(reportId, isValid);
    }
}
