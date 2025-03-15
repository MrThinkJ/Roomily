package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.entity.FindPartnerPost;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ChatRoomStatus;
import com.c2se.roomily.enums.ChatRoomType;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.enums.FindPartnerPostStatus;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.CreateFindPartnerPostRequest;
import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.payload.request.UpdateFindPartnerPostRequest;
import com.c2se.roomily.repository.FindPartnerPostRepository;
import com.c2se.roomily.repository.FindPartnerRequestRepository;
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
    private final FindPartnerRequestRepository findPartnerRequestRepository;
    private final UserService userService;
    private final RoomService roomService;
    private final ChatRoomService chatRoomService;
    private final NotificationService notificationService;

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
        FindPartnerPost findPartnerPost = FindPartnerPost.builder().title(request.getTitle()).content(
                request.getContent()).poster(user).currentPeople(users.size()).maxPeople(request.getMaxPeople()).status(
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
    public String requestToJoinFindPartnerPost(String userId, String findPartnerPostId, String chatRoomId) {
        FindPartnerPost findPartnerPost = getFindPartnerPostEntity(findPartnerPostId);
        if (!chatRoomService.isUserInChatRoom(userId, chatRoomId))
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "You are not in this chat room");
        int REQUEST_TTL = 5;
        String privateCode =  findPartnerRequestRepository.generateKey(userId, findPartnerPost.getId(), chatRoomId, REQUEST_TTL);
        chatRoomService.updateChatRoomStatus(chatRoomId, ChatRoomStatus.WAITING);
        return privateCode;
    }

    @Override
    public void cancelRequestToJoinFindPartnerPost(String userId, String privateCode) {
        String value = findPartnerRequestRepository.findByKey(privateCode);
        if (value == null) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "Invalid private code");
        }
        String[] parts = value.split("#");
        String savedUserId = parts[0];
        if (!savedUserId.equals(userId)) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "You are not the requester of this request");
        }
        String chatRoomId = parts[2];
        findPartnerRequestRepository.deleteByKey(privateCode);
        chatRoomService.updateChatRoomStatus(chatRoomId, ChatRoomStatus.ACTIVE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptRequestToJoinFindPartnerPost(String posterId, String privateCode) {
        String value = findPartnerRequestRepository.findByKey(privateCode);
        if (value == null) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "Invalid private code");
        }
        String[] parts = value.split("#");
        String userId = parts[0];
        String findPartnerPostId = parts[1];
        String chatRoomId = parts[2];
        FindPartnerPost findPartnerPost = getFindPartnerPostEntity(findPartnerPostId);
        if (!findPartnerPost.getPoster().getId().equals(posterId)) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "You are not the poster of this post");
        }
        User user = userService.getUserEntity(userId);
        findPartnerPost.getParticipants().add(user);
        findPartnerPost.setCurrentPeople(findPartnerPost.getCurrentPeople() + 1);
        findPartnerPostRepository.save(findPartnerPost);
        findPartnerRequestRepository.deleteByKey(privateCode);
        String chatRoomLandlordId = chatRoomService.getChatRoomIdByFindPartnerPostIdAndType(findPartnerPostId, ChatRoomType.GROUP);
        // If the chat room is already created, add the user to the chat room
        if (chatRoomLandlordId != null) {
            chatRoomService.addUserToGroupChatRoom(chatRoomLandlordId
                    ,user.getId());
            return;
        }

        checkEnoughParticipants(findPartnerPost);
        chatRoomService.updateChatRoomStatus(chatRoomId, ChatRoomStatus.COMPLETED);
    }

    @Override
    public void rejectRequestToJoinFindPartnerPost(String posterId, String privateCode) {
        String value = findPartnerRequestRepository.findByKey(privateCode);
        if (value == null) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "Invalid private code");
        }
        String[] parts = value.split("#");
        String findPartnerPostId = parts[1];
        String roomId = parts[2];
        FindPartnerPost findPartnerPost = getFindPartnerPostEntity(findPartnerPostId);
        if (!findPartnerPost.getPoster().getId().equals(posterId)) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "You are not the poster of this post");
        }
        findPartnerRequestRepository.deleteByKey(privateCode);
        chatRoomService.updateChatRoomStatus(roomId, ChatRoomStatus.ACTIVE);
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
        String chatRoomId = chatRoomService.getChatRoomIdByFindPartnerPostIdAndType(findPartnerPostId, ChatRoomType.GROUP);
        // If the chat room is already created, add the user to the chat room
        if (chatRoomId != null) {
            chatRoomService.addUserToGroupChatRoom(user.getId(), chatRoomId);
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
        findPartnerPost.setTitle(request.getTitle());
        findPartnerPost.setContent(request.getContent());
        findPartnerPost.setMaxPeople(request.getMaxPeople());
        findPartnerPostRepository.save(findPartnerPost);
    }

    private void checkEnoughParticipants(FindPartnerPost findPartnerPost) {
        if (findPartnerPost.getCurrentPeople() < findPartnerPost.getMaxPeople()) return;
        findPartnerPost.setStatus(FindPartnerPostStatus.FULL);
        Set<User> participants = findPartnerPost.getParticipants();
        participants.add(findPartnerPost.getRoom().getLandlord());
        ChatRoom chatRoom = chatRoomService.createGroupChatRoom(findPartnerPost.getPoster().getId(),
                                                       participants.stream().map(User::getId).collect(
                                                                        Collectors.toSet()),
                                                       "Find partner, room: " + findPartnerPost.getRoom().getId(),
                                                       findPartnerPost.getRoom().getId());
        String chatRoomId = chatRoom.getId();
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
