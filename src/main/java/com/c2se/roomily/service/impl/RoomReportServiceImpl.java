package com.c2se.roomily.service.impl;

import com.c2se.roomily.config.RabbitMQConfig;
import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.RoomReport;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.*;
import com.c2se.roomily.event.pojo.RoomDeleteEvent;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.CreateRoomReportRequest;
import com.c2se.roomily.payload.response.RoomReportResponse;
import com.c2se.roomily.repository.RentedRoomRepository;
import com.c2se.roomily.repository.RoomReportRepository;
import com.c2se.roomily.repository.RoomRepository;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.service.EventService;
import com.c2se.roomily.service.RoomReportService;
import com.c2se.roomily.service.RoomService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomReportServiceImpl implements RoomReportService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomReportRepository roomReportRepository;
    private final RentedRoomRepository rentedRoomRepository;
    private final RabbitTemplate rabbitTemplate;
    private final EventService eventService;

    @Override
    public List<RoomReportResponse> getAllRoomReports(String roomId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created_at"));
        return roomReportRepository.findAllByRoomId(roomId, pageable)
                .map(this::mapToRoomReportResponse)
                .getContent();
    }

    @Override
    public List<RoomReportResponse> getAllRoomReportsByStatus(String status, Integer page, Integer size) {
        try{
            List<RoomReport> reports =  roomReportRepository.findAllByStatus(ReportStatus.valueOf(status.toUpperCase()),
                                                                             PageRequest.of(page, size)).getContent();
            return reports.stream().map(this::mapToRoomReportResponse).toList();
        } catch (Exception e){
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "Invalid status");
        }
    }

    @Override
    public List<RoomReportResponse> getAllRoomReportsByRoomIdAndStatus(String roomId, String status, Integer page,
                                                                       Integer size) {
        try{
            List<RoomReport> reports = roomReportRepository.findAllByRoomIdAndStatus(roomId,
                                                                                     ReportStatus.valueOf(status.toUpperCase()),
                                                                                     PageRequest.of(page, size)).getContent();
            return reports.stream().map(this::mapToRoomReportResponse).toList();
        } catch (Exception e){
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "Invalid status");
        }
    }

    @Override
    public List<RoomReportResponse> getAllRoomReportsByReporterId(String reporterId, Integer page, Integer size) {
        try{
            List<RoomReport> reports = roomReportRepository.findAllByReporterId(reporterId,
                                                                                PageRequest.of(page, size)).getContent();
            return reports.stream().map(this::mapToRoomReportResponse).toList();
        } catch (Exception e){
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "Invalid status");
        }
    }

    @Override
    public void reportRoom(String reporterId, CreateRoomReportRequest createRoomReportRequest) {
        User reporter = userRepository.findById(reporterId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", reporterId));
        if (roomReportRepository.existsByReporterIdAndRoomId(reporterId, createRoomReportRequest.getRoomId())) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "You have already reported this room");
        }
        RoomReportType roomReportType;
        try {
            roomReportType = RoomReportType.valueOf(createRoomReportRequest.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "Invalid report type");
        }
        RoomReport roomReport = RoomReport.builder()
                .reporter(reporter)
                .room(roomRepository.findById(createRoomReportRequest.getRoomId()).orElseThrow(
                        () -> new ResourceNotFoundException("Room", "id", createRoomReportRequest.getRoomId())))
                .reason(createRoomReportRequest.getReason())
                .status(ReportStatus.PENDING)
                .type(roomReportType)
                .build();
        roomReportRepository.save(roomReport);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = {APIException.class})
    public void processReport(String reportId, Boolean isValid) {
        RoomReport roomReport = roomReportRepository.findById(reportId).orElseThrow(
                () -> new ResourceNotFoundException("RoomReport", "id", reportId));
        Room room = roomReport.getRoom();
        if (isValid) {
            if (roomReport.getRoom().getStatus() == RoomStatus.BANNED) {
                throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                       "This room has already been banned");
            }
            if (roomReport.getRoom().getStatus() == RoomStatus.DELETED) {
                throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                       "This room has already been deleted");
            }
            if (roomReport.getRoom().getStatus() == RoomStatus.RENTED) {
                throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                       "This room is currently rented and cannot be banned");
            }
            RentedRoom rentedRoom = rentedRoomRepository.findActiveByRoomId(room.getId(), List.of(RentedRoomStatus.IN_USE,
                                                                                            RentedRoomStatus.DEBT,
                                                                                            RentedRoomStatus.DEPOSIT_NOT_PAID,
                                                                                            RentedRoomStatus.BILL_MISSING,
                                                                                            RentedRoomStatus.PENDING));
            if (rentedRoom != null) {
                throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                       "Cannot delete room because it is currently rented");
            }
            room.setStatus(RoomStatus.BANNED);
            roomRepository.save(room);
            Map<String, String> body = new HashMap<>();
            body.put("room_id", room.getId());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,
                                          RabbitMQConfig.ROOM_ROUTING_KEY,
                                          body);
            eventService.publishEvent(RoomDeleteEvent.builder(this)
                                              .roomId(room.getId())
                                              .build());
            roomReportRepository.updateRoomReportStatusByRoomId(roomReport.getRoom().getId(), ReportStatus.PROCESSED);
        }
        roomReport.setStatus(ReportStatus.PROCESSED);
        roomReportRepository.save(roomReport);
    }

    private RoomReportResponse mapToRoomReportResponse(RoomReport roomReport) {
        return RoomReportResponse.builder()
                .id(roomReport.getId())
                .reporterId(roomReport.getReporter().getId())
                .roomId(roomReport.getRoom().getId())
                .reason(roomReport.getReason())
                .status(roomReport.getStatus().name())
                .createdAt(roomReport.getCreatedAt())
                .build();
    }
}
