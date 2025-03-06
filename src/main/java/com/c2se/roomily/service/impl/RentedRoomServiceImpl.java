package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.FindPartnerPost;
import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.enums.FindPartnerPostStatus;
import com.c2se.roomily.enums.RentedRoomStatus;
import com.c2se.roomily.enums.RoomStatus;
import com.c2se.roomily.event.DebtDateExpireEvent;
import com.c2se.roomily.event.RoomExpireEvent;
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
import com.c2se.roomily.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentedRoomServiceImpl implements RentedRoomService {
    private static final int RENT_REQUEST_TTL = 30;
    private final UserService userService;
    private final RoomService roomService;
    private final FindPartnerService findPartnerService;
    private final RentedRoomRepository rentedRoomRepository;
    private final RentRequestRepository rentRequestRepository;
    private final ChatRoomService chatRoomService;
    private final EventService eventService;

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
        User user = userService.getUserEntity(userId);
        Room room = roomService.getRoomEntityById(createRentedRoomRequest.getRoomId());
        if (room.getStatus() != RoomStatus.AVAILABLE)
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "This room is not available");
        CreateRentRequest request = CreateRentRequest.builder()
                .userId(userId)
                .roomId(createRentedRoomRequest.getRoomId())
                .startDate(createRentedRoomRequest.getStartDate())
                .build();
        if (createRentedRoomRequest.getFindPartnerPostId() == null) {
            request.setFindPartnerPostId(null);
            return rentRequestRepository.generateKey(userId, request, RENT_REQUEST_TTL);
        }
        FindPartnerPost findPartnerPost = findPartnerService.getFindPartnerPostEntity(
                createRentedRoomRequest.getFindPartnerPostId());
        if (!userId.equals(findPartnerPost.getPoster().getId()))
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                    "You are not the poster of find partner post");
        request.setFindPartnerPostId(createRentedRoomRequest.getFindPartnerPostId());
        return rentRequestRepository.generateKey(userId, request, RENT_REQUEST_TTL);
    }

    @Override
    public void cancelRentRequest(String userId, String privateCode) {
        String value = rentRequestRepository.findByKey(privateCode);
        String[] parts = value.split("#");
        if (!userId.equals(parts[0]))
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "You are not the requester");
        rentRequestRepository.deleteByKey(privateCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptRent(String landlordId, String privateCode) {
        String value = rentRequestRepository.findByKey(privateCode);
        String[] parts = value.split("#");
        String userId = parts[0];
        String roomId = parts[1];
        User user = userService.getUserEntity(userId);
        Room room = roomService.getRoomEntityById(roomId);
        if (room.getStatus() != RoomStatus.AVAILABLE)
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "This room is not available");
        if (!landlordId.equals(room.getLandlord().getId()))
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "You are not the landlord");
        LocalDate startDate = LocalDate.parse(parts[2]);
        String findPartnerPostId = parts.length > 3 ? parts[4] : null;
        RentedRoom rentedRoom = RentedRoom.builder()
                .user(user)
                .room(room)
                .landlord(userService.getUserEntity(landlordId))
                .startDate(startDate)
                .endDate(startDate.plusMonths(1))
                .status(RentedRoomStatus.IN_USE)
                .build();
        if (findPartnerPostId != null) {
            FindPartnerPost findPartnerPost = findPartnerService.getFindPartnerPostEntity(findPartnerPostId);
            findPartnerService.updateFindPartnerPostStatus(findPartnerPostId, FindPartnerPostStatus.COMPLETED.toString());
            findPartnerPost.getParticipants().remove(user);
            rentedRoom.setCoTenants(findPartnerPost.getParticipants());
        }
        rentedRoomRepository.save(rentedRoom);
        roomService.updateRoomStatus(roomId, RoomStatus.RENTED.toString());
        rentedRoom.setStatus(RentedRoomStatus.IN_USE);
        rentedRoomRepository.save(rentedRoom);
        findPartnerService.deleteFindPartnerPost(userId, findPartnerPostId);
        rentRequestRepository.deleteByKey(privateCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void denyRent(String landlordId, String privateCode) {
        String value = rentRequestRepository.findByKey(privateCode);
        String[] parts = value.split("#");
        String roomId = parts[1];
        String findPartnerPostId = parts.length > 3 ? parts[4] : null;
        Room room = roomService.getRoomEntityById(roomId);
        if (!landlordId.equals(room.getLandlord().getId()))
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "You are not the landlord");
        rentRequestRepository.deleteByKey(privateCode);
        if (findPartnerPostId != null) {
            FindPartnerPost findPartnerPost = findPartnerService.getFindPartnerPostEntity(findPartnerPostId);
            findPartnerService.deleteFindPartnerPost(findPartnerPost.getPoster().getId(), findPartnerPostId);
            String chatRoomId = chatRoomService.getChatRoomIdByFindPartnerPostId(findPartnerPostId);
            chatRoomService.deleteGroupChatRoom(findPartnerPost.getPoster().getId(), chatRoomId);
        }
    }

    @Override
    public void cancelRent(String userId, String roomId) {
    }

    @Override
    public void updateRentedRoom(String landlordId, String roomId, UpdateRentedRoomRequest updateRentedRoomRequest) {
        RentedRoom rentedRoom = rentedRoomRepository.findActiveByRoomId(roomId);
        if (rentedRoom == null)
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "This room is not rented");
        if (updateRentedRoomRequest.getStartDate() != null)
            rentedRoom.setStartDate(updateRentedRoomRequest.getStartDate());
        if (updateRentedRoomRequest.getEndDate() != null)
            rentedRoom.setEndDate(updateRentedRoomRequest.getEndDate());
        if (updateRentedRoomRequest.getStatus() != null)
            rentedRoom.setStatus(RentedRoomStatus.valueOf(updateRentedRoomRequest.getStatus()));
        rentedRoomRepository.save(rentedRoom);
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional(rollbackFor = Exception.class)
    public void scheduleEndDate() {
        List<RentedRoom> rooms = rentedRoomRepository.findByEndDate(LocalDate.now());
        rooms.forEach(rentedRoom -> eventService.publishEvent(RoomExpireEvent.builder()
                .rentedRoomId(rentedRoom.getId())
                .roomId(rentedRoom.getRoom().getId())
                .landlordId(rentedRoom.getLandlord().getId())
                .userId(rentedRoom.getUser().getId())
                .build()));
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional(rollbackFor = Exception.class)
    public void scheduleDebtDate() {
        List<RentedRoom> rooms = rentedRoomRepository.findByDebtDate(LocalDate.now());
        rooms.forEach(rentedRoom -> eventService.publishEvent(DebtDateExpireEvent.builder()
                .rentedRoomId(rentedRoom.getId())
                .roomId(rentedRoom.getRoom().getId())
                .landlordId(rentedRoom.getLandlord().getId())
                .userId(rentedRoom.getUser().getId())
                .build()));
    }

//    @Override
//    public void deleteRentedRoom(String landlordId, String userId, String roomId) {
//
//    }

    private RentedRoomResponse mapToRentedRoomResponse(RentedRoom rentedRoom) {
        return RentedRoomResponse.builder()
                .id(rentedRoom.getId())
                .startDate(rentedRoom.getStartDate())
                .endDate(rentedRoom.getEndDate())
                .status(rentedRoom.getStatus().toString())
                .createdAt(rentedRoom.getCreatedAt())
                .updatedAt(rentedRoom.getUpdatedAt())
                .roomId(rentedRoom.getRoom().getId())
                .userId(rentedRoom.getUser().getId())
                .landlordId(rentedRoom.getLandlord().getId())
                .build();
    }
}
