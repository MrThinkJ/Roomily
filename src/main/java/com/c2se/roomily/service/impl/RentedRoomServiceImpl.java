package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.*;
import com.c2se.roomily.enums.*;
import com.c2se.roomily.event.DebtDateExpireEvent;
import com.c2se.roomily.event.RoomExpireEvent;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.CreateRentedRoomRequest;
import com.c2se.roomily.payload.request.RentalRequest;
import com.c2se.roomily.payload.request.UpdateRentedRoomRequest;
import com.c2se.roomily.payload.response.RentedRoomResponse;
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
    private final UserService userService;
    private final RoomService roomService;
    private final FindPartnerService findPartnerService;
    private final RequestCacheService requestCacheService;
    private final RentedRoomRepository rentedRoomRepository;
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
    public RentalRequest requestRent(String userId, CreateRentedRoomRequest createRentedRoomRequest) {
        User user = userService.getUserEntity(userId);
        Room room = roomService.getRoomEntityById(createRentedRoomRequest.getRoomId());
        ChatRoom chatRoom = chatRoomService.getChatRoomEntity(createRentedRoomRequest.getChatRoomId());
        if (room.getStatus() != RoomStatus.AVAILABLE)
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "This room is not available");
        RentalRequest rentalRequest = RentalRequest.builder()
                .requesterId(userId)
                .recipientId(room.getLandlord().getId())
                .status(RequestStatus.PENDING)
                .build();
        RentalRequest savedRequest = requestCacheService.saveRequest(rentalRequest);
        chatRoom.setRequestId(savedRequest.getId());
        chatRoomService.saveChatRoom(chatRoom);
        return savedRequest;
    }

    @Override
    public void cancelRentRequest(String userId, String chatRoomId) {
        ChatRoom chatRoom = chatRoomService.getChatRoomEntity(chatRoomId);
        RentalRequest rentalRequest = requestCacheService.getRequest(chatRoom.getRequestId()).orElse(null);
        if (rentalRequest == null)
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "Invalid request id");
        if (!rentalRequest.getRequesterId().equals(userId))
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "You are not the requester");
        requestCacheService.removeRequest(chatRoom.getRequestId());
        chatRoom.setRequestId(null);
        chatRoom.setStatus(ChatRoomStatus.ACTIVE);
        chatRoomService.saveChatRoom(chatRoom);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptRent(String landlordId, String chatRoomId) {
        ChatRoom chatRoom = chatRoomService.getChatRoomEntity(chatRoomId);
        RentalRequest rentalRequest = requestCacheService.getRequest(chatRoom.getRequestId()).orElse(null);
        if (rentalRequest == null)
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "Invalid request id");
        if (!rentalRequest.getRecipientId().equals(landlordId))
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "You are not the recipient");

        User user = userService.getUserEntity(rentalRequest.getRequesterId());
        Room room = roomService.getRoomEntityById(chatRoom.getRoomId());

        if (room.getStatus() != RoomStatus.AVAILABLE)
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "This room is not available");
        if (!landlordId.equals(room.getLandlord().getId()))
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "You are not the landlord");

        RentedRoom rentedRoom = RentedRoom.builder()
                .user(user)
                .room(room)
                .landlord(userService.getUserEntity(landlordId))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .status(RentedRoomStatus.IN_USE)
                .rentedRoomWallet(BigDecimal.ZERO)
                .rentalDeposit(room.getRentalDeposit())
                .build();
        String findPartnerPostId = chatRoom.getFindPartnerPostId();
        if (findPartnerPostId != null) {
            FindPartnerPost findPartnerPost = findPartnerService.getFindPartnerPostEntity(findPartnerPostId);
            findPartnerService.updateFindPartnerPostStatus(findPartnerPostId,
                                                           FindPartnerPostStatus.COMPLETED.toString());
            findPartnerPost.getParticipants().remove(user);
            rentedRoom.setCoTenants(findPartnerPost.getParticipants());
            findPartnerService.deleteFindPartnerPost(findPartnerPost.getPoster().getId(), findPartnerPostId);
        } else {
            List<String> usersInChatRoom = chatRoomService.getChatRoomUserIds(chatRoomId);
            usersInChatRoom.remove(landlordId);
            usersInChatRoom.remove(rentalRequest.getRequesterId());
            rentedRoom.setCoTenants(userService.getUserEntities(usersInChatRoom));
        }

        chatRoomService.updateChatRoomStatus(chatRoomId, ChatRoomStatus.ACTIVE);
        rentedRoomRepository.save(rentedRoom);
        roomService.updateRoomStatus(chatRoom.getRoomId(), RoomStatus.RENTED.toString());
        rentedRoom.setStatus(RentedRoomStatus.IN_USE);
        rentedRoomRepository.save(rentedRoom);
        requestCacheService.removeRequest(chatRoom.getRequestId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRent(String landlordId, String chatRoomId) {
        ChatRoom chatRoom = chatRoomService.getChatRoomEntity(chatRoomId);
        RentalRequest rentalRequest = requestCacheService.getRequest(chatRoom.getRequestId()).orElse(null);
        if (rentalRequest == null)
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "Invalid request id");
        String findPartnerPostId = chatRoom.getFindPartnerPostId();
        Room room = roomService.getRoomEntityById(chatRoom.getRoomId());
        if (!landlordId.equals(room.getLandlord().getId()))
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "You are not the landlord");
        requestCacheService.removeRequest(chatRoom.getRequestId());
        chatRoomService.updateChatRoomStatus(chatRoom.getId(), ChatRoomStatus.CANCELED);
        if (findPartnerPostId != null) {
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
