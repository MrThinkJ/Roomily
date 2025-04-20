package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.*;
import com.c2se.roomily.enums.*;
import com.c2se.roomily.event.pojo.DebtDateExpireEvent;
import com.c2se.roomily.event.pojo.DepositPayEvent;
import com.c2se.roomily.event.pojo.RoomExpireEvent;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.*;
import com.c2se.roomily.payload.response.CheckoutResponse;
import com.c2se.roomily.payload.response.RentedRoomResponse;
import com.c2se.roomily.repository.RentedRoomRepository;
import com.c2se.roomily.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final ChatMessageService chatMessageService;
    private final EventService eventService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ContractGenerationService contractGenerationService;
    private final NotificationService notificationService;
    private final RentedRoomActivityService rentedRoomActivityService;

    private final List<RentedRoomStatus> activeStatus = List.of(RentedRoomStatus.IN_USE,
                                                                RentedRoomStatus.DEBT,
                                                                RentedRoomStatus.DEPOSIT_NOT_PAID,
                                                                RentedRoomStatus.BILL_MISSING,
                                                                RentedRoomStatus.PENDING);

    @Override
    public RentedRoom getRentedRoomEntityById(String rentedRoomId) {
        return rentedRoomRepository.findById(rentedRoomId).orElseThrow(
                () -> new ResourceNotFoundException("RentedRoom", "id", rentedRoomId));
    }

    @Override
    public void saveRentedRoom(RentedRoom rentedRoom) {
        rentedRoomRepository.save(rentedRoom);
    }

    @Override
    public RentedRoomResponse getRentedRoomActiveByUserIdOrCoTenantIdAndRoomId(String userId, String roomId) {
        RentedRoom rentedRoom = rentedRoomRepository.findActiveByRoomIdAndUserIdOrCoTenantId(roomId, userId, activeStatus);
        if (rentedRoom == null) {
            return null;
        }
        return mapToRentedRoomResponse(rentedRoom);
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
    public void deleteRentedRoomNotPaidDepositByRoomId(String roomId) {
        rentedRoomRepository.deleteByRoomIdAndStatus(roomId, RentedRoomStatus.DEPOSIT_NOT_PAID);
    }

    @Override
    public RentedRoomResponse getActiveRentedRoomByRoomId(String roomId) {
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
                .roomId(room.getId())
                .findPartnerPostId(createRentedRoomRequest.getFindPartnerPostId())
                .chatRoomId(chatRoom.getId())
                .build();
        RentalRequest savedRequest = requestCacheService.saveRequest(rentalRequest);
        chatRoom.setRequestId(savedRequest.getId());
        chatRoom.setStatus(ChatRoomStatus.WAITING);
        ChatMessage chatMessage = ChatMessage.builder()
                .message("Bạn có yêu cầu thuê phòng từ " + user.getFullName())
                .chatRoom(chatRoom)
                .subId(chatRoom.getNextSubId() + 1)
                .build();
        chatRoom.setLastMessage(chatMessage.getMessage());
        chatRoom.setLastMessageTimeStamp(LocalDateTime.now());
        chatRoom.setNextSubId(chatRoom.getNextSubId() + 1);
        chatMessageService.saveSystemMessage(chatMessage, chatRoom);
        chatRoomService.saveChatRoom(chatRoom);
        simpMessagingTemplate.convertAndSendToUser(room.getLandlord().getId(), "/queue/chat-room",
                                                   chatRoom.getId());
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
        ChatMessage chatMessage = ChatMessage.builder()
                .message("Yêu cầu thuê phòng đã bị hủy")
                .chatRoom(chatRoom)
                .subId(chatRoom.getNextSubId() + 1)
                .build();
        chatRoom.setLastMessage(chatMessage.getMessage());
        chatRoom.setLastMessageTimeStamp(LocalDateTime.now());
        chatRoom.setNextSubId(chatRoom.getNextSubId() + 1);
        chatMessageService.saveSystemMessage(chatMessage, chatRoom);
        chatRoomService.saveChatRoom(chatRoom);
        simpMessagingTemplate.convertAndSendToUser(rentalRequest.getRequesterId(), "/queue/chat-room",
                                                   chatRoom.getId());
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
        Room room = roomService.getRoomEntityById(rentalRequest.getRoomId());

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
                .status(RentedRoomStatus.DEPOSIT_NOT_PAID)
                .rentedRoomWallet(BigDecimal.ZERO)
                .rentalDeposit(BigDecimal.ZERO)
                .walletDebt(BigDecimal.ZERO)
                .build();

        String findPartnerPostId = rentalRequest.getFindPartnerPostId();
        // If this is a rented group, remove the find partner post and update the rented group
        if (findPartnerPostId != null) {
            FindPartnerPost findPartnerPost = findPartnerService.getFindPartnerPostEntity(findPartnerPostId);
            findPartnerService.updateFindPartnerPostStatus(findPartnerPostId,
                                                           FindPartnerPostStatus.COMPLETED.toString());
            findPartnerPost.getParticipants().remove(user);
            rentedRoom.setCoTenants(findPartnerPost.getParticipants());
            findPartnerService.deleteFindPartnerPost(findPartnerPost.getPoster().getId(), findPartnerPostId);
        }
        findPartnerService.deleteActiveFindPartnerPostByRoomId(room.getId());
        RentedRoom savedRentedRoom = rentedRoomRepository.save(rentedRoom);
        contractGenerationService.generateRentContract(savedRentedRoom);

        handleRentalDepositPayment(savedRentedRoom, chatRoom, user.getId());
        requestCacheService.removeRequest(chatRoom.getRequestId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRent(String landlordId, String chatRoomId) {
        ChatRoom chatRoom = chatRoomService.getChatRoomEntity(chatRoomId);
        RentalRequest rentalRequest = requestCacheService.getRequest(chatRoom.getRequestId()).orElse(null);
        if (rentalRequest == null)
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "Invalid request id");
        String findPartnerPostId = rentalRequest.getFindPartnerPostId();
        Room room = roomService.getRoomEntityById(rentalRequest.getRoomId());
        if (!landlordId.equals(room.getLandlord().getId()))
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "You are not the landlord");
        chatRoomService.updateChatRoomStatus(chatRoom.getId(), ChatRoomStatus.CANCELED);
        if (findPartnerPostId != null) {
            FindPartnerPost findPartnerPost = findPartnerService.getFindPartnerPostEntity(findPartnerPostId);
            findPartnerService.deleteFindPartnerPost(findPartnerPost.getPoster().getId(), findPartnerPostId);
            chatRoomService.archiveAllChatRoomsByFindPartnerPostId(findPartnerPostId);
        }
        ChatMessage chatMessage = ChatMessage.builder()
                .message("Yêu cầu thuê phòng đã bị hủy bởi chủ trọ")
                .chatRoom(chatRoom)
                .subId(chatRoom.getNextSubId() + 1)
                .build();
        chatRoom.setLastMessage(chatMessage.getMessage());
        chatRoom.setLastMessageTimeStamp(LocalDateTime.now());
        chatRoom.setNextSubId(chatRoom.getNextSubId() + 1);
        chatMessageService.saveSystemMessage(chatMessage, chatRoom);
        chatRoomService.saveChatRoom(chatRoom);
        simpMessagingTemplate.convertAndSendToUser(rentalRequest.getRequesterId(), "/queue/chat-room",
                                                   chatRoom.getId());
        requestCacheService.removeRequest(chatRoom.getRequestId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exitRent(String userId, String rentedRoomId) {
        RentedRoom rentedRoom = getRentedRoomEntityById(rentedRoomId);
        
        // Verify user is part of this rental (main tenant or co-tenant)
        boolean isMainTenant = rentedRoom.getUser().getId().equals(userId);
        boolean isCoTenant = rentedRoom.getCoTenants().stream().anyMatch(tenant -> tenant.getId().equals(userId));
        
        if (!isMainTenant && !isCoTenant) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, 
                "You are not a tenant of this rental");
        }
        
        if (isMainTenant) {
            // Case 1: Main tenant exits
            if (rentedRoom.getCoTenants().isEmpty()) {
                // If no co-tenants, this is effectively a full cancellation
                handleRentedRoomCancellation(rentedRoom);
                return;
            }
            
            // Promote first co-tenant to main tenant
            User newMainTenant = rentedRoom.getCoTenants().iterator().next();
            User previousMainTenant = rentedRoom.getUser();
            rentedRoom.getCoTenants().remove(newMainTenant);
            rentedRoom.setUser(newMainTenant);
            // Create activity and notification
            String message = "Người dùng " + previousMainTenant.getFullName() + " đã rời khỏi với tư cách người thuê chính. Người dùng " +
                             newMainTenant.getFullName() + " hiện là người thuê chính.";
            CreateRentedRoomActivityRequest activityRequest = CreateRentedRoomActivityRequest.builder()
                            .rentedRoomId(rentedRoom.getId())
                            .message(message)
                            .build();
            rentedRoomActivityService.createRentedRoomActivity(activityRequest);
            notifyRentalParticipants(rentedRoom, "Người thuê chính thay đổi", message);
        } else {
            // Case 2: Co-tenant exits
            User exitingUser = userService.getUserEntity(userId);
            rentedRoom.getCoTenants().remove(exitingUser);
            
            // Create activity and notification
            String message = "Người dùng " + userId + " đã rời khỏi với tư cách người thuê cùng.";
            CreateRentedRoomActivityRequest activityRequest = CreateRentedRoomActivityRequest.builder()
                            .rentedRoomId(rentedRoom.getId())
                            .message(message)
                            .build();
            rentedRoomActivityService.createRentedRoomActivity(activityRequest);
            notifyRentalParticipants(rentedRoom, "Người thuê cùng rời khỏi", message);
        }
        
        // Save the updated rental
        rentedRoomRepository.save(rentedRoom);

        // Remove this user from group chat room if exist
        ChatRoom chatRoom = chatRoomService.getChatRoomByRentedRoomId(rentedRoomId);
        if (chatRoom != null) {
            chatRoomService.removeUserFromGroupChatRoom(chatRoom.getManagerId(), chatRoom.getId(), userId);
        }
    }

    private void notifyRentalParticipants(RentedRoom rentedRoom, String header, String message) {
        // Collect all participants (main tenant, co-tenants, and landlord)
        Set<User> allParticipants = new HashSet<>(rentedRoom.getCoTenants());
        allParticipants.add(rentedRoom.getUser());

        // Send notification to each participant
        for (User participant : allParticipants) {
            CreateNotificationRequest notification = CreateNotificationRequest.builder()
                    .header(header)
                    .body(message)
                    .userId(participant.getId())
                    .extra(rentedRoom.getId())
                    .build();
            notificationService.sendNotification(notification);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRent(String userId, String rentedRoomId) {
        RentedRoom rentedRoom = getRentedRoomEntityById(rentedRoomId);
        User user = userService.getUserEntity(userId);

        // If user is tenant
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_TENANT"))){

            // Verify user is the main tenant (only main tenant can cancel entire rental)
            if (!rentedRoom.getUser().getId().equals(userId)) {
                throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                       "Only the main tenant can cancel the entire rental");
            }

            // Check rental status - allow cancellation only before move-in or deposit
            if (rentedRoom.getStatus() != RentedRoomStatus.DEPOSIT_NOT_PAID) {
                throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                       "Cannot cancel active rental. Please contact landlord for early termination.");
            }
        }

        // If user is landlord
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_LANDLORD"))){

            // Verify the landlord owns this rental
            if (!rentedRoom.getLandlord().getId().equals(userId)) {
                throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                       "You are not the landlord of this rental");
            }
        }
        handleRentedRoomCancellation(rentedRoom);
    }

    private void handleRentedRoomCancellation(RentedRoom rentedRoom) {
        // 1. Archive the chat room associated with the rental
        ChatRoom chatRoom = chatRoomService.getChatRoomByRentedRoomId(rentedRoom.getId());
        if (chatRoom != null) {
            List<User> chatRoomUsers = chatRoomService.getChatRoomUsers(chatRoom.getId());
            for (User user : chatRoomUsers) {
                CreateNotificationRequest notification = CreateNotificationRequest.builder()
                    .header("Rental Cancelled")
                    .body("The rental for room " + rentedRoom.getRoom().getId() + " has been cancelled")
                    .userId(user.getId())
                    .build();
                notificationService.sendNotification(notification);
            }
            chatRoomService.updateChatRoomStatus(chatRoom.getId(), ChatRoomStatus.ARCHIVED);
            ChatMessage chatMessage = ChatMessage.builder()
                .message("Rental has been cancelled")
                .chatRoom(chatRoom)
                .subId(chatRoom.getNextSubId() + 1)
                .build();
            chatMessageService.saveSystemMessage(chatMessage, chatRoom);
        }

        // 2. Update room status back to available
        Room room = rentedRoom.getRoom();
        roomService.updateRoomStatus(rentedRoom.getRoom().getId(), RoomStatus.AVAILABLE.toString());

        // 3. Delete any active find partner posts for additional tenants (rented group need to find more partners)
        FindPartnerPost additionalTenantPost = findPartnerService.getAdditionalTenantFindPartnerPostEntityByRoomId(
                room.getId());
        findPartnerService.deleteFindPartnerPost(additionalTenantPost.getPoster().getId(),
                                                 additionalTenantPost.getId());

        // 4. Update rented room status
        rentedRoom.setStatus(RentedRoomStatus.END);
        rentedRoom.setEndDate(LocalDate.now());

        // 5. Refund rental deposit (if any)
        if (rentedRoom.getRentalDeposit().compareTo(BigDecimal.ZERO) > 0) {
            User mainTenant = rentedRoom.getUser();
            BigDecimal depositAmount = rentedRoom.getRentalDeposit();
            mainTenant.setBalance(mainTenant.getBalance().add(depositAmount));
            // Update user's wallet balance
            userService.saveUser(mainTenant);

            // Log the transaction and notify the user
            CreateNotificationRequest depositRefundNotification = CreateNotificationRequest.builder()
                    .header("Hoàn trả tiền cọc")
                    .body("Số tiền cọc: " + rentedRoom.getRentalDeposit() + " đã được hoàn trả vào tài khoản của bạn")
                    .userId(mainTenant.getId())
                    .extra(rentedRoom.getId())
                    .build();
            notificationService.sendNotification(depositRefundNotification);

            // Reset the deposit amount in the rented room entity
            rentedRoom.setRentalDeposit(BigDecimal.ZERO);
            rentedRoomRepository.save(rentedRoom);
        }

        // 6. Create rental activity record (optional)
//        CreateRentedRoomActivityRequest activityRequest = CreateRentedRoomActivityRequest.builder()
//            .rentedRoomId(rentedRoom.getId())
//            .message("Rental was cancelled")
//            .build();
//        rentedRoomActivityService.createRentedRoomActivity(activityRequest);

        // 7. Handle any scheduled events
//        eventService.cancelAllEventsForRentedRoom(rentedRoom.getId());

        // 8. Notify all participants
        Set<User> allParticipants = new HashSet<>(rentedRoom.getCoTenants());
        allParticipants.add(rentedRoom.getUser());
        allParticipants.add(rentedRoom.getLandlord());
        for (User participant : allParticipants) {
            CreateNotificationRequest notification = CreateNotificationRequest.builder()
                .header("Rental Cancelled")
                .body("The rental for room " + rentedRoom.getRoom().getId() + " has been cancelled")
                .userId(participant.getId())
                .build();
            notificationService.sendNotification(notification);
        }
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
    public void mockPublishRoomExpireEvent(String rentedRoomId) {
        RentedRoom rentedRoom = rentedRoomRepository.findById(rentedRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("RentedRoom", "id", rentedRoomId));
        eventService.publishEvent(RoomExpireEvent.builder(this)
                                          .rentedRoomId(rentedRoom.getId())
                                          .roomId(rentedRoom.getRoom().getId())
                                          .landlordId(rentedRoom.getLandlord().getId())
                                          .userId(rentedRoom.getUser().getId())
                                          .build());
    }

    @Override
    public void mockPublishDebtDateExpireEvent(String rentedRoomId) {
        RentedRoom rentedRoom = rentedRoomRepository.findById(rentedRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("RentedRoom", "id", rentedRoomId));
        eventService.publishEvent(DebtDateExpireEvent.builder(this)
                                          .rentedRoomId(rentedRoom.getId())
                                          .roomId(rentedRoom.getRoom().getId())
                                          .landlordId(rentedRoom.getLandlord().getId())
                                          .userId(rentedRoom.getUser().getId())
                                          .build());
    }

    @Override
    public boolean isUserRentedRoomBefore(String userId, String roomId) {
        return rentedRoomRepository.existsByRoomIdAndUserIdOrCoTenantId(roomId, userId);
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional(rollbackFor = Exception.class)
    public void scheduleEndDate() {
        List<RentedRoom> rooms = rentedRoomRepository.findByEndDate(LocalDate.now());
        rooms.forEach(rentedRoom -> eventService.publishEvent(RoomExpireEvent.builder(this)
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
        rooms.forEach(rentedRoom -> eventService.publishEvent(DebtDateExpireEvent.builder(this)
                                                                      .rentedRoomId(rentedRoom.getId())
                                                                      .roomId(rentedRoom.getRoom().getId())
                                                                      .landlordId(rentedRoom.getLandlord().getId())
                                                                      .userId(rentedRoom.getUser().getId())
                                                                      .build()));
    }

    private void handleRentalDepositPayment(RentedRoom rentedRoom, ChatRoom chatRoom, String requesterId) {
        if (rentedRoom.getStatus() == RentedRoomStatus.DEPOSIT_NOT_PAID) {
            eventService.publishEvent(DepositPayEvent.builder(this)
                                              .rentedRoom(rentedRoom)
                                              .chatRoom(chatRoom)
                                              .requesterId(requesterId)
                                              .build());
        }

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
                .walletDebt(rentedRoom.getWalletDebt().toString())
                .rentedRoomWallet(rentedRoom.getRentedRoomWallet().toString())
                .rentalDeposit(rentedRoom.getRentalDeposit().toString())
                .build();
    }
}
