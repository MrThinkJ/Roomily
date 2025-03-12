package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.FindPartnerPost;
import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.*;
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
import com.c2se.roomily.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
    private final List<RentedRoomStatus> activeStatus = List.of(RentedRoomStatus.IN_USE, RentedRoomStatus.DEBT);

    @Override
    public RentedRoom getRentedRoomEntityById(String roomId) {
        return rentedRoomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("RentedRoom", "id", roomId));
    }

    @Override
    public void saveRentedRoom(RentedRoom rentedRoom) {
        rentedRoomRepository.save(rentedRoom);
    }

    @Override
    public RentedRoomResponse getRentedRoomActiveByUserIdOrCoTenantIdAndRoomId(String userId, String roomId) {
        return mapToRentedRoomResponse(rentedRoomRepository.findActiveByRoomIdAndUserIdOrCoTenantId(roomId, userId,
                                                                                                    activeStatus));
    }

    @Override
    public List<RentedRoomResponse> getRentedRoomActiveByUserIdOrCoTenantId(String userId) {
        List<RentedRoom> rentedRooms = rentedRoomRepository.findActiveByUserId(userId, activeStatus);
        rentedRooms.addAll(rentedRoomRepository.findActiveByCoTenantId(userId, activeStatus));
        return rentedRooms.stream().map(this::mapToRentedRoomResponse).collect(Collectors.toList());
    }

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
        return mapToRentedRoomResponse(rentedRoomRepository.findActiveByRoomId(roomId, activeStatus));
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
                .chatRoomId(createRentedRoomRequest.getChatRoomId())
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
        if (value == null)
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "Invalid private code");
        String[] parts = value.split("#");
        String userId = parts[0];
        String roomId = parts[1];
        String chatRoomId = parts[4];
        User user = userService.getUserEntity(userId);
        Room room = roomService.getRoomEntityById(roomId);

        // TODO: Remove this, this just for testing
        landlordId = room.getLandlord().getId();

        if (room.getStatus() != RoomStatus.AVAILABLE)
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "This room is not available");
        if (!landlordId.equals(room.getLandlord().getId()))
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "You are not the landlord");
        LocalDate startDate = LocalDate.parse(parts[2]);
        String findPartnerPostId = parts.length > 4 ? parts[3] : null;
        RentedRoom rentedRoom = RentedRoom.builder()
                .user(user)
                .room(room)
                .landlord(userService.getUserEntity(landlordId))
                .startDate(startDate)
                .endDate(startDate.plusMonths(1))
                .status(RentedRoomStatus.IN_USE)
                .rentedRoomWallet(BigDecimal.ZERO)
                .rentalDeposit(room.getRentalDeposit())
                .build();
        if (!findPartnerPostId.equals("null")) {
            FindPartnerPost findPartnerPost = findPartnerService.getFindPartnerPostEntity(findPartnerPostId);
            findPartnerService.updateFindPartnerPostStatus(findPartnerPostId,
                                                           FindPartnerPostStatus.COMPLETED.toString());
            findPartnerPost.getParticipants().remove(user);
            rentedRoom.setCoTenants(findPartnerPost.getParticipants());
            findPartnerService.deleteFindPartnerPost(userId, findPartnerPostId);
        } else {
            List<String> usersInChatRoom = chatRoomService.getChatRoomUserIds(chatRoomId);
            usersInChatRoom.remove(landlordId);
            usersInChatRoom.remove(userId);
            rentedRoom.setCoTenants(userService.getUserEntities(usersInChatRoom));
        }
        chatRoomService.updateChatRoomStatus(chatRoomId, ChatRoomStatus.ACTIVE);
        rentedRoomRepository.save(rentedRoom);
        roomService.updateRoomStatus(roomId, RoomStatus.RENTED.toString());
        rentedRoom.setStatus(RentedRoomStatus.IN_USE);
        rentedRoomRepository.save(rentedRoom);
        rentRequestRepository.deleteByKey(privateCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void denyRent(String landlordId, String privateCode) {
        String value = rentRequestRepository.findByKey(privateCode);
        if (value == null)
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "Invalid private code");
        String[] parts = value.split("#");
        String roomId = parts[1];
        String findPartnerPostId = parts.length > 3 ? parts[3] : null;
        Room room = roomService.getRoomEntityById(roomId);

        // TODO: Remove this, this just for testing
        landlordId = room.getLandlord().getId();

        if (!landlordId.equals(room.getLandlord().getId()))
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "You are not the landlord");
        rentRequestRepository.deleteByKey(privateCode);
        chatRoomService.updateChatRoomStatus(parts[4], ChatRoomStatus.CANCELED);
        if (!findPartnerPostId.equals("null")) {
            FindPartnerPost findPartnerPost = findPartnerService.getFindPartnerPostEntity(findPartnerPostId);
            findPartnerService.deleteFindPartnerPost(findPartnerPost.getPoster().getId(), findPartnerPostId);
            chatRoomService.archiveAllChatRoomsByFindPartnerPostId(findPartnerPostId);
        }
    }

    @Override
    public void cancelRent(String userId, String roomId) {
    }

    @Override
    public void updateRentedRoom(String landlordId, String roomId, UpdateRentedRoomRequest updateRentedRoomRequest) {
        RentedRoom rentedRoom = rentedRoomRepository.findActiveByRoomId(roomId,
                                                                        List.of(RentedRoomStatus.IN_USE,
                                                                                RentedRoomStatus.DEBT,
                                                                                RentedRoomStatus.PENDING));
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

    @Override
    public boolean isUserRentedRoomBefore(String userId, String roomId) {
        return rentedRoomRepository.existsByRoomIdAndUserIdOrCoTenantId(roomId, userId);
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
