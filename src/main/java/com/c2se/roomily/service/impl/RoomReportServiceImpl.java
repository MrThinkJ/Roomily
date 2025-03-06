package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.RoomReport;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.enums.ReportStatus;
import com.c2se.roomily.enums.RoomStatus;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.CreateRoomReportRequest;
import com.c2se.roomily.repository.RoomReportRepository;
import com.c2se.roomily.repository.RoomRepository;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.service.RoomReportService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RoomReportServiceImpl implements RoomReportService {
    RoomRepository roomRepository;
    UserRepository userRepository;
    RoomReportRepository roomReportRepository;

    @Override
    public void reportRoom(String reporterId, CreateRoomReportRequest createRoomReportRequest) {
        User reporter = userRepository.findById(reporterId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", reporterId));
        if (roomReportRepository.existsByReporterIdAndRoomId(reporterId, createRoomReportRequest.getRoomId())) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                    "You have already reported this room");
        }
        RoomReport roomReport = RoomReport.builder()
                .reporter(reporter)
                .room(roomRepository.findById(createRoomReportRequest.getRoomId()).orElseThrow(
                        () -> new ResourceNotFoundException("Room", "id", createRoomReportRequest.getRoomId())))
                .reason(createRoomReportRequest.getReason())
                .build();
        roomReportRepository.save(roomReport);
    }

    @Override
    public void processReport(String reportId, Boolean isValid) {
        RoomReport roomReport = roomReportRepository.findById(reportId).orElseThrow(
                () -> new ResourceNotFoundException("RoomReport", "id", reportId));
        if (isValid) {
            roomRepository.updateRoomStatusById(roomReport.getRoom().getId(), RoomStatus.BANNED);
            roomReportRepository.updateRoomReportStatusByRoomId(roomReport.getRoom().getId(), ReportStatus.PROCESSED);
        }
        roomReport.setStatus(ReportStatus.PROCESSED);
        roomReportRepository.save(roomReport);
    }
}
