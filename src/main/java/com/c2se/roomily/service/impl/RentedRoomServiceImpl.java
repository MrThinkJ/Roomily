package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.enums.RentedRoomStatus;
import com.c2se.roomily.enums.RoomStatus;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.CreateRentRequest;
import com.c2se.roomily.payload.request.CreateRentedRoomRequest;
import com.c2se.roomily.payload.request.UpdateRentedRoomRequest;
import com.c2se.roomily.payload.response.RentedRoomResponse;
import com.c2se.roomily.repository.RentRequestRepository;
import com.c2se.roomily.repository.RentedRoomRepository;
import com.c2se.roomily.repository.RoomRepository;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.service.RentedRoomService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RentedRoomServiceImpl implements RentedRoomService {
    UserRepository userRepository;
    RoomRepository roomRepository;
    RentedRoomRepository rentedRoomRepository;
    RentRequestRepository rentRequestRepository;
    @Override
    public List<RentedRoomResponse> getRentedRoomsByLandlordId(String landlordId) {
        return rentedRoomRepository.findByLandlordId(landlordId).stream()
                .map(this::mapToRentedRoomResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RentedRoomResponse> getRentedRoomHistoryByRoomId(String roomId) {
        return rentedRoomRepository.findByRoomId(roomId).stream().map(this::mapToRentedRoomResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RentedRoomResponse getRentedRoomByRoomId(String roomId) {
        return mapToRentedRoomResponse(rentedRoomRepository.findActiveByRoomId(roomId));
    }

    @Override
    public List<RentedRoomResponse> getRentedRoomsByUserId(String userId) {
        return rentedRoomRepository.findByUserId(userId).stream().map(this::mapToRentedRoomResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String requestRent(String userId, CreateRentedRoomRequest createRentedRoomRequest) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", "userId", userId));
        Room room = roomRepository.findById(createRentedRoomRequest.getRoomId()).orElseThrow(
                () -> new ResourceNotFoundException("Room", "roomId", createRentedRoomRequest.getRoomId()));
        if (room.getStatus() != RoomStatus.AVAILABLE)
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "This room is not available");
        String privateCode = UUID.randomUUID().toString();
        CreateRentRequest request = CreateRentRequest.builder()
                .userId(userId)
                .roomId(createRentedRoomRequest.getRoomId())
                .landlordId(room.getLandlord().getId())
                .startDate(createRentedRoomRequest.getStartDate())
                .endDate(createRentedRoomRequest.getEndDate())
                .privateCode(privateCode)
                .createdAt(LocalDateTime.now())
                .build();
        rentRequestRepository.save(userId, request);
        return room.getId()+ "." +privateCode;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptRent(String landlordId, String userId, String privateCode) {
        String roomId = privateCode.split("\\.")[0];
        String checkCode = privateCode.split("\\.")[1];
        CreateRentRequest rentRequest = rentRequestRepository.findByUserId(userId);
        if (rentRequest == null || !rentRequest.getPrivateCode().equals(checkCode))
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "Invalid private code");
        Room room = roomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("Room", "roomId", roomId));
        if (!room.getLandlord().getId().equals(landlordId))
            throw new APIException(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, "Not Authorized");
        if (room.getStatus() != RoomStatus.AVAILABLE)
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "This room is not pending");
        RentedRoom rentedRoom = RentedRoom.builder()
                .user(userRepository.findById(userId).orElseThrow(
                        () -> new ResourceNotFoundException("User", "userId", userId)))
                .room(room)
                .landlord(userRepository.findById(landlordId).orElseThrow(
                        () -> new ResourceNotFoundException("User", "userId", landlordId)))
                .startDate(LocalDateTime.parse(rentRequest.getStartDate()))
                .endDate(LocalDateTime.parse(rentRequest.getEndDate()))
                .status(RentedRoomStatus.RENTED)
                .build();
        rentedRoomRepository.save(rentedRoom);
        room.setStatus(RoomStatus.RENTED);
        roomRepository.save(room);
        rentedRoom.setStatus(RentedRoomStatus.RENTED);
        rentedRoomRepository.save(rentedRoom);
    }

    @Override
    public void cancelRent(String landlordId, String roomId) {
        RentedRoom rentedRoom = rentedRoomRepository.findActiveByRoomId(roomId);
        if (rentedRoom == null)
            throw new ResourceNotFoundException("RentedRoom", "roomId", roomId);
        if (!rentedRoom.getLandlord().getId().equals(landlordId))
            throw new APIException(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, "You are not the landlord of this room");
        if (rentedRoom.getStatus() != RentedRoomStatus.RENTED)
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "This room is not rented");
        rentedRoom.setStatus(RentedRoomStatus.CANCELLED);
        rentedRoomRepository.save(rentedRoom);
    }

    @Override
    public void updateRentedRoom(String landlordId, String roomId, UpdateRentedRoomRequest updateRentedRoomRequest) {

    }


//    @Override
//    public void deleteRentedRoom(String landlordId, String userId, String roomId) {
//
//    }

    private RentedRoomResponse mapToRentedRoomResponse(RentedRoom rentedRoom) {
        return RentedRoomResponse.builder()
                .id(rentedRoom.getId())
                .startDate(rentedRoom.getStartDate().toString())
                .endDate(rentedRoom.getEndDate().toString())
                .status(rentedRoom.getStatus().toString())
                .createdAt(rentedRoom.getCreatedAt().toString())
                .updatedAt(rentedRoom.getUpdatedAt().toString())
                .roomId(rentedRoom.getRoom().getId())
                .userId(rentedRoom.getUser().getId())
                .landlordId(rentedRoom.getLandlord().getId())
                .build();
    }
}
