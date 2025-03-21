package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.entity.FindPartnerPost;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.*;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.CreateFindPartnerPostRequest;
import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.payload.request.RentalRequest;
import com.c2se.roomily.payload.request.UpdateFindPartnerPostRequest;
import com.c2se.roomily.payload.response.ChatRoomResponse;
import com.c2se.roomily.repository.FindPartnerPostRepository;
import com.c2se.roomily.service.*;
import lombok.RequiredArgsConstructor;
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

    @Override
    public FindPartnerPost getFindPartnerPostEntity(String findPartnerPostId) {
        return findPartnerPostRepository.findById(findPartnerPostId).orElseThrow(
                () -> new ResourceNotFoundException("FindPartnerPost", "id", findPartnerPostId));
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
        if (findPartnerPostRepository.existsByPosterId(user.getId())) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "You have already created a find partner post");
        }
        Set<User> users = request.getCurrentParticipantPrivateIds().size() == 0 ?
                new HashSet<>() : userService.getUserEntitiesByPrivateIds(request.getCurrentParticipantPrivateIds());

        if (users.size() != request.getCurrentParticipantPrivateIds().size()) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "Some participants are not found");
        }
        users.add(user);
        FindPartnerPost findPartnerPost = FindPartnerPost.builder().poster(user).currentPeople(users.size()).maxPeople(request.getMaxPeople()).status(
                FindPartnerPostStatus.ACTIVE).participants(users).room(room).build();
        findPartnerPostRepository.save(findPartnerPost);
    }

    @Override
    public void deleteFindPartnerPostByRoomId(String roomId) {
        List<FindPartnerPost> findPartnerPosts = findPartnerPostRepository.findByRoomId(roomId);
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
        RentalRequest rentalRequest = requestCacheService.getRequest(chatRoom.getRequestId()).orElse(null);
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
        User user = userService.getUserEntity(rentalRequest.getRequesterId());
        findPartnerPost.getParticipants().add(user);
        findPartnerPost.setCurrentPeople(findPartnerPost.getCurrentPeople() + 1);
        findPartnerPostRepository.save(findPartnerPost);
        requestCacheService.removeRequest(chatRoomId);
        ChatRoom chatRoomLandlord = chatRoomService.getChatRoomByFindPartnerPostIdAndType(
                findPartnerPost.getId(), ChatRoomType.GROUP);
        // If the chat room is already created, add the user to the chat room
        if (chatRoomLandlord != null) {
            chatRoomService.addUserToGroupChatRoom(chatRoomLandlord.getId(), user.getId());
            return;
        }
        checkEnoughParticipants(findPartnerPost);
        chatRoomService.updateChatRoomStatus(chatRoomId, ChatRoomStatus.COMPLETED);
        requestCacheService.removeRequest(chatRoom.getRequestId());
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
        findPartnerPostRepository.save(findPartnerPost);
        ChatRoom chatRoom = chatRoomService.getDirectChatRoomByUserIds(user.getId(), findPartnerPost.getPoster().getId());
        if (chatRoom != null) {
            chatRoom.setStatus(ChatRoomStatus.ACTIVE);
            chatRoom.setRequestId(null);
            chatRoomService.saveChatRoom(chatRoom);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFindPartnerPost(String userId, String findPartnerPostId) {
        User user = userService.getUserEntity(userId);
        FindPartnerPost findPartnerPost = getFindPartnerPostEntity(findPartnerPostId);
        if (!findPartnerPost.getPoster().getId().equals(userId)) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "You are not the poster of this post");
        }
        findPartnerPost.getParticipants().forEach(participant -> {
            CreateNotificationRequest createNotificationRequest = CreateNotificationRequest.builder().header(
                    "Find partner post has been deleted").body("Find partner post has been deleted").userId(
                    participant.getId()).type("FIND_PARTNER_POST_DELETED").build();
            notificationService.sendNotification(createNotificationRequest);
        });
        chatRoomService.archiveAllChatRoomsByFindPartnerPostId(findPartnerPostId);
        findPartnerPostRepository.delete(findPartnerPost);
    }

    @Override
    public void exitWhenChatRoomIsFull(String userId, String findPartnerPostId) {
        User user = userService.getUserEntity(userId);
        FindPartnerPost findPartnerPost = getFindPartnerPostEntity(findPartnerPostId);
        if (!findPartnerPost.getParticipants().contains(user)) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "This user is not in this post");
        }
        findPartnerPost.getParticipants().remove(user);
        findPartnerPost.setCurrentPeople(findPartnerPost.getCurrentPeople() - 1);
        findPartnerPost.setStatus(FindPartnerPostStatus.ACTIVE);
        findPartnerPostRepository.save(findPartnerPost);
        chatRoomService.removeUserFromGroupChatRoom(
                findPartnerPost.getPoster().getId(),
                findPartnerPost.getChatRoomId(),
                user.getId());
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
        if (findPartnerPost.getCurrentPeople() < findPartnerPost.getMaxPeople()) return;
        findPartnerPost.setStatus(FindPartnerPostStatus.FULL);
        Set<User> participants = findPartnerPost.getParticipants();
        participants.add(findPartnerPost.getRoom().getLandlord());
        ChatRoomResponse chatRoom = chatRoomService.createGroupChatRoom(findPartnerPost.getPoster().getId(),
                                                                        participants.stream().map(User::getId).collect(
                                                                                Collectors.toSet()),
                                                                        "Find partner, room: " + findPartnerPost.getRoom().getId(),
                                                                        findPartnerPost.getRoom().getId());
        String chatRoomId = chatRoom.getChatRoomId();
        participants.forEach(participant -> {
            CreateNotificationRequest createNotificationRequest = CreateNotificationRequest.builder().header(
                    "You have been added to a group chat room").body("You have been added to a group chat room").userId(
                    participant.getId()).type("CHAT").extra(chatRoomId).build();
            notificationService.sendNotification(createNotificationRequest);
        });
        findPartnerPost.setChatRoomId(chatRoomId);
        findPartnerPostRepository.save(findPartnerPost);
    }
}
