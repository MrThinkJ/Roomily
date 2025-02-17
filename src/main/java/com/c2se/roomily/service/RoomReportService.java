package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.CreateRoomReportRequest;

public interface RoomReportService {
    void reportRoom(String reporterId, CreateRoomReportRequest createRoomReportRequest);
    Boolean hasAlreadyReported(String reporterId, String roomId);
    void processReport(String reportId, Boolean isValid);
}
