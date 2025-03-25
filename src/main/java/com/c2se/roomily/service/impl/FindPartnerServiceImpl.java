package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.*;
import com.c2se.roomily.enums.*;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.CreateFindPartnerPostRequest;
import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.payload.request.RentalRequest;
import com.c2se.roomily.payload.request.UpdateFindPartnerPostRequest;
import com.c2se.roomily.payload.response.ChatRoomResponse;
import com.c2se.roomily.payload.response.FindPartnerPostResponse;
import com.c2se.roomily.payload.response.UserInFindPartnerPostResponse;
import com.c2se.roomily.repository.FindPartnerPostRepository;
import com.c2se.roomily.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FindPartnerServiceImpl implements FindPartnerService {
    private final FindPartnerPostRepository findPartnerPostRepository;
    private final UserService userService;
    private final RoomService roomService;
    private final ChatRoomService chatRoomService;
    private final NotificationService notificationService;
    private final RequestCacheService requestCacheService;
    private final RentedRoomOperationsService rentedRoomOperationsService;

    @Override
    public FindPartnerPost getFindPartnerPostEntity(String findPartnerPostId) {
        return findPartnerPostRepository.findById(findPartnerPostId).orElseThrow(
                () -> new ResourceNotFoundException("FindPartnerPost", "id", findPartnerPostId));
    }

    @Override
    public FindPartnerPost getAdditionalTenantFindPartnerPostEntityByRoomId(String roomId) {
        return findPartnerPostRepository.findByRoomIdAndType(roomId, FindPartnerPostType.ADDITIONAL_TENANT)
                .orElse(null);
    }

    @Override
    public List<FindPartnerPostResponse> getActiveFindPartnerPostsByRoomId(String roomId) {
        return findPartnerPostRepository.findByRoomIdAndStatus(roomId, FindPartnerPostStatus.ACTIVE).stream()
                .map(this::mapToFindPartnerPostResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean isUserInActiveFindPartnerPostOfRoom(String userId, String roomId) {
        return findPartnerPostRepository.findActiveByUserIdInParticipants(userId, FindPartnerPostStatus.ACTIVE).stream()
                .anyMatch(findPartnerPost -> findPartnerPost.getRoom().getId().equals(roomId));
    }

    @Override
    public List<FindPartnerPostResponse> getActiveFindPartnerPostsByUserId(String userId) {
        return findPartnerPostRepository.findActiveByUserIdInParticipants(userId, FindPartnerPostStatus.ACTIVE).stream()
                .map(this::mapToFindPartnerPostResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void updateFindPartnerPostStatus(String findPartnerPostId, String status) {
        FindPartnerPost findPartnerPost = getFindPartnerPostEntity(findPartnerPostId);
        findPartnerPost.setStatus(FindPartnerPostStatus.valueOf(status));
        findPartnerPostRepository.save(findPartnerPost);
    }

    @Override
    public void createFindPartnerPost(String userId, CreateFindPartnerPostRequest request) {
        User user = userService.getUserEntity(userId);
        Room room = roomService.getRoomEntityById(request.getRoomId());
        // If the room is in find partner only mode, reject the request
        if (room.getStatus().equals(RoomStatus.FIND_PARTNER_ONLY))
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "This room is in find partner only mode, you cannot create a find partner post for it");
        // If the user has already created a find partner post, reject the request
        if (findPartnerPostRepository.existsByPosterId(user.getId())) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "You have already created a find partner post");
        }
        // Validate max people
        if (request.getMaxPeople() > room.getMaxPeople())
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "Max people must be less than or equal to room's max people");
        // Validate request type and permissions
        FindPartnerPostType type = request.getType() != null ? request.getType() : FindPartnerPostType.NEW_RENTAL;
        RentedRoom rentedRoom = null;
        // Additional tenant requests
        if (type == FindPartnerPostType.ADDITIONAL_TENANT) {
            // Verify rented room ID is provided
            if (request.getRentedRoomId() == null) {
                throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                       "Rented room ID is required for additional tenant requests");
            }
            rentedRoom = rentedRoomOperationsService.getRentedRoomById(request.getRentedRoomId());
            // Verify user is a tenant in the rented room
            boolean isUserTenant = rentedRoom.getUser().getId().equals(userId) ||
                                 rentedRoom.getCoTenants().stream()
                                         .anyMatch(tenant -> tenant.getId().equals(userId));
            if (!isUserTenant) {
                throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                       "You must be a tenant to create an additional tenant request");
            }
            // Verify the room matches
            if (!rentedRoom.getRoom().getId().equals(request.getRoomId())) {
                throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                       "Room ID must match the rented room");
            }
            // Verify no existing find partner post for this rented room
            if (findPartnerPostRepository.existsByRentedRoomIdAndType(request.getRentedRoomId(), type)) {
                throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                       "A find partner post for this rented room already exists");
            }
            roomService.setRoomFindPartnerOnly(request.getRoomId());
        }
        Set<User> users = request.getCurrentParticipantPrivateIds().size() == 0 ?
                new HashSet<>() : userService.getUserEntitiesByPrivateIds(request.getCurrentParticipantPrivateIds());
        if (users.size() != request.getCurrentParticipantPrivateIds().size()) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, 
                                   "Some participants are not found");
        }
        users.add(user);
        FindPartnerPost findPartnerPost = FindPartnerPost.builder()
                .poster(user)
                .currentPeople(users.size())
                .maxPeople(request.getMaxPeople())
                .status(FindPartnerPostStatus.ACTIVE)
                .participants(users)
                .room(room)
                .type(type)
                .rentedRoom(rentedRoom)
                .build();
        findPartnerPostRepository.save(findPartnerPost);
    }

    @Override
    public void deleteActiveFindPartnerPostByRoomId(String roomId) {
        List<FindPartnerPost> findPartnerPosts = findPartnerPostRepository.findByRoomIdAndStatus(
                roomId,FindPartnerPostStatus.ACTIVE);
        findPartnerPosts.forEach(findPartnerPost -> {
            findPartnerPost.getParticipants().forEach(participant -> {
                CreateNotificationRequest createNotificationRequest = CreateNotificationRequest.builder().header(
                        "Find partner post has been deleted").body("Find partner post has been deleted").userId(
                        participant.getId()).type("FIND_PARTNER_POST_DELETED").build();
                notificationService.sendNotification(createNotificationRequest);
            });
            chatRoomService.archiveAllChatRoomsByFindPartnerPostId(findPartnerPost.getId());
            findPartnerPostRepository.delete(findPartnerPost);
        });
    }

    @Override
    public RentalRequest requestToJoinFindPartnerPost(String userId, String findPartnerPostId, String chatRoomId) {
        FindPartnerPost findPartnerPost = getFindPartnerPostEntity(findPartnerPostId);
        ChatRoom chatRoom = chatRoomService.getChatRoomEntity(chatRoomId);
        if (!chatRoomService.isUserInChatRoom(userId, chatRoomId))
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "You are not in this chat room");
        RentalRequest rentalRequest = RentalRequest.builder()
                .requesterId(userId)
                .recipientId(findPartnerPost.getPoster().getId())
                .status(RequestStatus.PENDING)
                .findPartnerPostId(findPartnerPostId)
                .build();
        RentalRequest savedRequest = requestCacheService.saveRequest(rentalRequest);
        chatRoom.setRequestId(savedRequest.getId());
        chatRoomService.updateChatRoomStatus(chatRoomId, ChatRoomStatus.WAITING);
        return savedRequest;
    }

    @Override
    public void cancelRequestToJoinFindPartnerPost(String userId, String chatRoomId) {
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
    public void acceptRequestToJoinFindPartnerPost(String posterId, String chatRoomId) {
        ChatRoom chatRoom = chatRoomService.getChatRoomEntity(chatRoomId);
        RentalRequest rentalRequest = validateAndGetRentalRequest(chatRoom, posterId);
        FindPartnerPost findPartnerPost = validateAndGetFindPartnerPost(rentalRequest, posterId);
        User user = userService.getUserEntity(rentalRequest.getRequesterId());

        addParticipantToPost(findPartnerPost, user);
        handleExistingChatRoom(findPartnerPost, user);
        finalizeAcceptance(chatRoom, findPartnerPost);
    }

    @Override
    public void rejectRequestToJoinFindPartnerPost(String posterId, String chatRoomId) {
        RentalRequest rentalRequest = requestCacheService.getRequest(chatRoomId).orElse(null);
        if (rentalRequest == null) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "Invalid request id");
        }
        if (!rentalRequest.getRecipientId().equals(posterId)) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "You are not the recipient");
        }
        FindPartnerPost findPartnerPost = getFindPartnerPostEntity(rentalRequest.getFindPartnerPostId());
        if (!findPartnerPost.getPoster().getId().equals(posterId)) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "You are not the poster of this post");
        }
        requestCacheService.removeRequest(chatRoomId);
        chatRoomService.updateChatRoomStatus(chatRoomId, ChatRoomStatus.ACTIVE);
    }

    @Override
    public void addParticipantToFindPartnerPost(String userId, String findPartnerPostId, String privateId) {
        User user = userService.getUserEntityByPrivateId(privateId);
        User poster = userService.getUserEntity(userId);
        FindPartnerPost findPartnerPost = getFindPartnerPostEntity(findPartnerPostId);
        if (!findPartnerPost.getPoster().getId().equals(poster.getId())) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "You are not the poster of this post");
        }
        findPartnerPost.getParticipants().add(user);
        findPartnerPost.setCurrentPeople(findPartnerPost.getCurrentPeople() + 1);
        findPartnerPostRepository.save(findPartnerPost);
        ChatRoom chatRoom = chatRoomService.getChatRoomByFindPartnerPostIdAndType(findPartnerPostId,
                                                                                  ChatRoomType.GROUP);
        // If the chat room is already created, add the user to the chat room
        if (chatRoom != null) {
            chatRoomService.addUserToGroupChatRoom(user.getId(), chatRoom.getId());
            return;
        }
        checkEnoughParticipants(findPartnerPost);
    }

    @Override
    public void removeParticipantFromFindPartnerPost(String userId, String findPartnerPostId, String participantId) {
        User user = userService.getUserEntity(userId);
        User participant = userService.getUserEntity(participantId);
        FindPartnerPost findPartnerPost = getFindPartnerPostEntity(findPartnerPostId);
        if (!findPartnerPost.getPoster().getId().equals(user.getId())) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "You are not the poster of this post");
        }
        findPartnerPost.getParticipants().remove(participant);
        findPartnerPost.setCurrentPeople(findPartnerPost.getCurrentPeople() - 1);
        if (findPartnerPost.getChatRoomId() != null) {
            chatRoomService.removeUserFromGroupChatRoom(user.getId(), findPartnerPost.getChatRoomId(), participantId);
        }
        if (findPartnerPost.getStatus() != FindPartnerPostStatus.FULL) {
            findPartnerPost.setStatus(FindPartnerPostStatus.ACTIVE);
        }
        findPartnerPostRepository.save(findPartnerPost);
    }

    @Override
    public void exitFindPartnerPost(String userId, String findPartnerPostId) {
        User user = userService.getUserEntity(userId);
        FindPartnerPost findPartnerPost = getFindPartnerPostEntity(findPartnerPostId);
        if (!findPartnerPost.getParticipants().contains(user)) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "You are not in this post");
        }
        findPartnerPost.getParticipants().remove(user);
        findPartnerPost.setCurrentPeople(findPartnerPost.getCurrentPeople() - 1);
        ChatRoom chatRoom = chatRoomService.getDirectChatRoomByUserIds(user.getId(),
                                                                       findPartnerPost.getPoster().getId());
        if (chatRoom != null) {
            chatRoom.setStatus(ChatRoomStatus.ACTIVE);
            chatRoom.setRequestId(null);
            chatRoomService.saveChatRoom(chatRoom);
        }
        if (findPartnerPost.getChatRoomId() != null) {
            chatRoomService.removeUserFromGroupChatRoom(findPartnerPost.getPoster().getId(),
                                                        findPartnerPost.getChatRoomId(),
                                                        userId);
        }
        if (findPartnerPost.getStatus() != FindPartnerPostStatus.FULL) {
            findPartnerPost.setStatus(FindPartnerPostStatus.ACTIVE);
        }
        findPartnerPostRepository.save(findPartnerPost);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFindPartnerPost(String userId, String findPartnerPostId) {
        FindPartnerPost findPartnerPost = validatePostOwnership(userId, findPartnerPostId);
        sendDeletionNotifications(findPartnerPost);
        cleanupChatRooms(findPartnerPost.getId());
        findPartnerPostRepository.delete(findPartnerPost);
    }

    @Override
    public void updateFindPartnerPost(String userId, String findPartnerPostId,
                                      UpdateFindPartnerPostRequest request) {
        User user = userService.getUserEntity(userId);
        FindPartnerPost findPartnerPost = getFindPartnerPostEntity(findPartnerPostId);
        if (!findPartnerPost.getPoster().getId().equals(user.getId())) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "You are not the poster of this post");
        }
        findPartnerPost.setMaxPeople(request.getMaxPeople());
        findPartnerPostRepository.save(findPartnerPost);
    }

    private void checkEnoughParticipants(FindPartnerPost findPartnerPost) {
        if (findPartnerPost.getCurrentPeople() < findPartnerPost.getMaxPeople())
            return;
        markPostAsFull(findPartnerPost);
        Set<User> participants = collectAllParticipants(findPartnerPost);
        String chatRoomId;
        if (findPartnerPost.getType() == FindPartnerPostType.ADDITIONAL_TENANT) {
            // For additional tenants, add them to the existing rental's chat room if it exists
            RentedRoom rentedRoom = findPartnerPost.getRentedRoom();
            ChatRoom existingChatRoom = chatRoomService.getChatRoomByRentedRoomId(rentedRoom.getId());
            if (existingChatRoom != null) {
                // Add new participants to existing chat room
                participants.forEach(participant -> {
                    if (!chatRoomService.isUserInChatRoom(participant.getId(), existingChatRoom.getId())) {
                        chatRoomService.addUserToGroupChatRoom(participant.getId(), existingChatRoom.getId());
                    }
                });
                chatRoomId = existingChatRoom.getId();
            } else {
                // Create new chat room if none exists
                chatRoomId = createGroupChatRoomForParticipants(findPartnerPost, participants);
            }
            // Update rented room with new co-tenants
            rentedRoom.getCoTenants().addAll(findPartnerPost.getParticipants());
            rentedRoomOperationsService.saveRentedRoom(rentedRoom);
        } else {
            chatRoomId = createGroupChatRoomForParticipants(findPartnerPost, participants);
        }

        sendChatRoomNotifications(participants, chatRoomId);
        updatePostWithChatRoomId(findPartnerPost, chatRoomId);
    }

    private void markPostAsFull(FindPartnerPost findPartnerPost) {
        findPartnerPost.setStatus(FindPartnerPostStatus.FULL);
    }

    private Set<User> collectAllParticipants(FindPartnerPost findPartnerPost) {
        Set<User> participants = findPartnerPost.getParticipants();
        participants.add(findPartnerPost.getRoom().getLandlord());
        return participants;
    }

    private String createGroupChatRoomForParticipants(FindPartnerPost findPartnerPost, Set<User> participants) {
        ChatRoomResponse chatRoom = chatRoomService.createGroupChatRoom(
                findPartnerPost.getPoster().getId(),
                participants.stream().map(User::getId).collect(Collectors.toSet()),
                "Find partner, room: " + findPartnerPost.getRoom().getId(),
                findPartnerPost.getRoom().getId());
        return chatRoom.getChatRoomId();
    }

    private void sendChatRoomNotifications(Set<User> participants, String chatRoomId) {
        participants.forEach(participant -> {
            CreateNotificationRequest createNotificationRequest = CreateNotificationRequest.builder()
                    .header("You have been added to a group chat room")
                    .body("You have been added to a group chat room")
                    .userId(participant.getId())
                    .type("CHAT")
                    .extra(chatRoomId)
                    .build();
            notificationService.sendNotification(createNotificationRequest);
        });
    }

    private void updatePostWithChatRoomId(FindPartnerPost findPartnerPost, String chatRoomId) {
        findPartnerPost.setChatRoomId(chatRoomId);
        findPartnerPostRepository.save(findPartnerPost);
    }

    private FindPartnerPostResponse mapToFindPartnerPostResponse(FindPartnerPost findPartnerPost) {
        return FindPartnerPostResponse.builder()
                .findPartnerPostId(findPartnerPost.getId())
                .currentPeople(findPartnerPost.getCurrentPeople())
                .maxPeople(findPartnerPost.getMaxPeople())
                .status(findPartnerPost.getStatus().name())
                .posterId(findPartnerPost.getPoster().getId())
                .roomId(findPartnerPost.getRoom().getId())
                .participants(findPartnerPost.getParticipants().stream()
                                      .map(user -> UserInFindPartnerPostResponse.builder()
                                              .userId(user.getId())
                                              .fullName(user.getFullName())
                                              .address(user.getAddress())
                                              .gender(user.getGender() == null ? null : (user.getGender() ?
                                                      "Nam" : "Nữ"))
                                              .build()
                                      ).collect(Collectors.toList()))
                .rentedRoomId(findPartnerPost.getRentedRoom() == null ? null : findPartnerPost.getRentedRoom().getId())
                .build();
    }

    private RentalRequest validateAndGetRentalRequest(ChatRoom chatRoom, String posterId) {
        RentalRequest rentalRequest = requestCacheService.getRequest(chatRoom.getRequestId()).orElse(null);
        if (rentalRequest == null) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "Invalid request id");
        }
        if (!rentalRequest.getRecipientId().equals(posterId)) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "You are not the recipient");
        }
        return rentalRequest;
    }

    private FindPartnerPost validateAndGetFindPartnerPost(RentalRequest rentalRequest, String posterId) {
        FindPartnerPost findPartnerPost = getFindPartnerPostEntity(rentalRequest.getFindPartnerPostId());
        if (!findPartnerPost.getPoster().getId().equals(posterId)) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "You are not the poster of this post");
        }
        return findPartnerPost;
    }

    private void addParticipantToPost(FindPartnerPost findPartnerPost, User user) {
        findPartnerPost.getParticipants().add(user);
        findPartnerPost.setCurrentPeople(findPartnerPost.getCurrentPeople() + 1);
        findPartnerPostRepository.save(findPartnerPost);
    }

    private void handleExistingChatRoom(FindPartnerPost findPartnerPost, User user) {
        ChatRoom chatRoomLandlord = chatRoomService.getChatRoomByFindPartnerPostIdAndType(
                findPartnerPost.getId(), ChatRoomType.GROUP);
        if (chatRoomLandlord != null) {
            chatRoomService.addUserToGroupChatRoom(chatRoomLandlord.getId(), user.getId());
        } else {
            checkEnoughParticipants(findPartnerPost);
        }
    }

    private void finalizeAcceptance(ChatRoom chatRoom, FindPartnerPost findPartnerPost) {
        chatRoomService.updateChatRoomStatus(chatRoom.getId(), ChatRoomStatus.COMPLETED);
        requestCacheService.removeRequest(chatRoom.getRequestId());
    }

    private FindPartnerPost validatePostOwnership(String userId, String findPartnerPostId) {
        FindPartnerPost findPartnerPost = getFindPartnerPostEntity(findPartnerPostId);
        if (!findPartnerPost.getPoster().getId().equals(userId)) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "You are not the poster of this post");
        }
        return findPartnerPost;
    }

    private void sendDeletionNotifications(FindPartnerPost findPartnerPost) {
        findPartnerPost.getParticipants().forEach(participant -> {
            CreateNotificationRequest createNotificationRequest = CreateNotificationRequest.builder()
                    .header("Find partner post has been deleted")
                    .body("Find partner post has been deleted")
                    .userId(participant.getId())
                    .type("FIND_PARTNER_POST_DELETED")
                    .build();
            notificationService.sendNotification(createNotificationRequest);
        });
    }

    private void cleanupChatRooms(String findPartnerPostId) {
        chatRoomService.archiveAllChatRoomsByFindPartnerPostId(findPartnerPostId);
    }
}
