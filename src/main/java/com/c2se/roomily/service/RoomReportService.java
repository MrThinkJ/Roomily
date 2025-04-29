package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.CreateRoomReportRequest;
import com.c2se.roomily.payload.response.RoomReportResponse;

import java.util.List;

public interface RoomReportService {
    List<RoomReportResponse> getAllRoomReports(String roomId, Integer page, Integer size);

    List<RoomReportResponse> getAllRoomReportsByStatus(String status, Integer page, Integer size);

    List<RoomReportResponse> getAllRoomReportsByRoomIdAndStatus(String roomId, String status, Integer page, Integer size);

    List<RoomReportResponse> getAllRoomReportsByReporterId(String reporterId, Integer page, Integer size);

    void reportRoom(String reporterId, CreateRoomReportRequest createRoomReportRequest);

    void processReport(String reportId, Boolean isValid);
}
