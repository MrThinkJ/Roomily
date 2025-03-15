package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.entity.ChatRoomUser;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ChatRoomStatus;
import com.c2se.roomily.enums.ChatRoomType;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.enums.RoomStatus;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.internal.ChatRoomUserData;
import com.c2se.roomily.payload.response.ChatRoomResponse;
import com.c2se.roomily.payload.response.ConversationResponse;
import com.c2se.roomily.repository.ChatRoomRepository;
import com.c2se.roomily.repository.ChatRoomUserRepository;
import com.c2se.roomily.service.ChatRoomService;
import com.c2se.roomily.service.RoomService;
import com.c2se.roomily.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {
    ChatRoomRepository chatRoomRepository;
    ChatRoomUserRepository chatRoomUserRepository;
    RoomService roomService;
    UserService userService;
    SimpMessagingTemplate messagingTemplate;

    @Override
    public ChatRoom getChatRoomEntity(String chatRoomId) {
        return chatRoomRepository.findById(chatRoomId).orElseThrow(
                () -> new ResourceNotFoundException("ChatRoom", "Id", chatRoomId));
    }

    @Override
    public void saveChatRoom(ChatRoom chatRoom) {
        chatRoomRepository.save(chatRoom);
    }

    @Override
    public void updateChatRoomStatus(String chatRoomId, ChatRoomStatus status) {
        chatRoomRepository.updateStatusById(chatRoomId, status);
    }

    @Override
    public void archiveAllChatRoomsByFindPartnerPostId(String findPartnerPostId) {
        chatRoomRepository.archiveAllByFindPartnerPostId(findPartnerPostId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatRoom createGroupChatRoom(String managerId, Set<String> userIds, String chatRoomName, String roomId) {
        userIds.add(managerId);

        Set<User> users = userService.getUserEntities(List.copyOf(userIds));
        if (users.size() != userIds.size()) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "Invalid user ids");
        }
        ChatRoom chatRoom = ChatRoom.builder()
                .name(chatRoomName)
                .managerId(managerId)
                .type(ChatRoomType.GROUP)
                .roomId(roomId)
                .status(ChatRoomStatus.ACTIVE)
                .nextSubId(0)
                .build();
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        List<ChatRoomUser> chatRoomUsers = users.stream().map(user -> ChatRoomUser.builder()
                .chatRoom(savedChatRoom)
                .user(user)
                .unreadMessageCount(0)
                .build()).toList();
        chatRoomUserRepository.saveAll(chatRoomUsers);
        users.forEach(user -> notifyNewChatRoom(savedChatRoom, user.getId()));
        return savedChatRoom;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatRoom getOrCreateDirectChatRoom(String userId1, String userId2, String findPartnerPostId) {
        String chatKey = generateDirectChatKey(userId1, userId2);
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findByChatKey(chatKey);
        if (existingChatRoom.isPresent()) {
            ChatRoom chatRoom = existingChatRoom.get();
            if (findPartnerPostId != null && chatRoom.getFindPartnerPostId() == null) {
                chatRoom.setFindPartnerPostId(findPartnerPostId);
                chatRoomRepository.save(chatRoom);
            }
            return chatRoom;
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .chatKey(chatKey)
                .name("DM_" + userId1 + "_" + userId2)
                .type(ChatRoomType.DIRECT)
                .status(ChatRoomStatus.ACTIVE)
                .findPartnerPostId(findPartnerPostId)
                .nextSubId(0)
                .build();

        chatRoomRepository.save(chatRoom);
        Set<User> users = userService.getUserEntities(List.of(userId1, userId2));
        if (users.size() != 2)
            throw new IllegalArgumentException("Invalid user ids");
        if (!chatRoomRepository.existsById(chatRoom.getId())) {
            throw new IllegalArgumentException("Invalid room id");
        }
        List<ChatRoomUser> chatRoomUsers = users.stream().map(user -> ChatRoomUser.builder()
                .chatRoom(chatRoom)
                .user(user)
                .unreadMessageCount(0)
                .build()).toList();
        chatRoomUserRepository.saveAll(chatRoomUsers);
        users.forEach(user -> notifyNewChatRoom(chatRoom, user.getId()));
        return chatRoom;
    }

    @Override
    public ChatRoom createDirectChatRoomToLandlord(String userId, String roomId) {
        Room room = roomService.getRoomEntityById(roomId);
        User landlord = userService.getUserEntity(room.getLandlord().getId());
        User tenant = userService.getUserEntity(userId);
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findByRoomIdAndUsers(
                roomId,
                landlord.getId(),
                tenant.getId()
        );

        if (existingChatRoom.isPresent()) {
            return existingChatRoom.get();
        }
        StringBuilder chatRoomName = new StringBuilder("DM_");
        chatRoomName.append(tenant.getFullName()).append("_").append(landlord.getFullName());
        Set<String> userIds = new HashSet<>();
        userIds.add(landlord.getId());
        userIds.add(tenant.getId());
        return createGroupChatRoom(landlord.getId(),
                                   userIds,
                                   chatRoomName.toString(), roomId);
    }

    @Override
    public void addUserToGroupChatRoom(String chatRoomId, String userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(
                () -> new ResourceNotFoundException("ChatRoom", "Id", chatRoomId));
        if (chatRoom.getType() != ChatRoomType.GROUP) {
            throw new IllegalArgumentException("Invalid chat room type");
        }
        User user = userService.getUserEntity(userId);
        ChatRoomUser chatRoomUser = ChatRoomUser.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();
        notifyNewChatRoom(chatRoom, userId);
        chatRoomUserRepository.save(chatRoomUser);
    }

    @Override
    public void removeUserFromGroupChatRoom(String managerId, String chatRoomId, String userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(
                () -> new ResourceNotFoundException("ChatRoom", "Id", chatRoomId));
        if (chatRoom.getType() != ChatRoomType.GROUP) {
            throw new IllegalArgumentException("Invalid chat room type");
        }
        if (!chatRoom.getManagerId().equals(managerId))
            throw new IllegalArgumentException("Only manager can remove user from group chat room");
        User user = userService.getUserEntity(userId);
        ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomAndUser(chatRoom, user).orElseThrow(
                () -> new ResourceNotFoundException("ChatRoom", "ChatRoom Or User", chatRoomId + " " + userId)
        );

        chatRoomUserRepository.delete(chatRoomUser);
    }

    @Override
    public void exitGroupChatRoom(String chatRoomId, String userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(
                () -> new ResourceNotFoundException("ChatRoom", "Id", chatRoomId));
        if (chatRoom.getType() != ChatRoomType.GROUP) {
            throw new IllegalArgumentException("Invalid chat room type");
        }
        User user = userService.getUserEntity(userId);
        ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomAndUser(chatRoom, user).orElseThrow(
                () -> new ResourceNotFoundException("ChatRoom", "ChatRoom Or User", chatRoomId + " " + userId)
        );
        chatRoomUserRepository.delete(chatRoomUser);
    }

    @Override
    public void deleteGroupChatRoom(String managerId, String roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("ChatRoom", "Id", roomId));
        if (chatRoom.getType() != ChatRoomType.GROUP) {
            throw new IllegalArgumentException("Invalid chat room type");
        }
        if (!chatRoom.getManagerId().equals(managerId))
            throw new IllegalArgumentException("Only manager can delete group chat room");
        chatRoomUserRepository.deleteByChatRoomId(chatRoom.getId());
        chatRoomRepository.delete(chatRoom);
    }

    @Override
    public String getChatRoomIdByFindPartnerPostIdAndType(String findPartnerPostId, ChatRoomType chatRoomType) {
        ChatRoom chatRoom = chatRoomRepository.findByFindPartnerPostIdAndType(findPartnerPostId, chatRoomType);
        if (chatRoom != null){
            return chatRoom.getId();
        }
        return null;
    }

    @Override
    public List<User> getChatRoomUsers(String roomId) {
        // TODO: After implement user service, implement this method, return user list of chat room, can return ids only
        return null;
    }

    @Override
    public List<String> getChatRoomUserIds(String chatRoomId) {
        getChatRoomEntity(chatRoomId);
        return chatRoomUserRepository.findUserIdInChatRoomByChatRoomId(chatRoomId);
    }

    @Override
    public List<ConversationResponse> getChatRoomsByUserId(String userId) {
        List<ChatRoomUserData> chatRoomUserData = chatRoomUserRepository.findDataByUserId(userId);
        if (chatRoomUserData.isEmpty()) {
            return List.of();
        }
        return chatRoomUserData.stream().map(data -> ConversationResponse.builder()
                .chatRoomId(data.getChatRoomId())
                .roomName(data.getRoomName())
                .lastMessage(data.getLastMessage())
                .lastMessageTime(data.getLastMessageTimeStamp())
                .lastMessageSender(data.getLastMessageSender())
                .unreadCount(data.getUnreadCount())
                .isGroup(data.getType() == ChatRoomType.GROUP)
                .build()).collect(Collectors.toList());
    }

    @Override
    public ChatRoomResponse getChatRoomInfo(String chatRoomId) {
        ChatRoom chatRoom = getChatRoomEntity(chatRoomId);
        return ChatRoomResponse.builder()
                .chatRoomId(chatRoom.getId())
                .roomName(chatRoom.getName())
                .managerId(chatRoom.getManagerId())
                .chatRoomType(chatRoom.getType().name())
                .chatRoomStatus(chatRoom.getStatus().name())
                .roomId(chatRoom.getRoomId())
                .findPartnerPostId(chatRoom.getFindPartnerPostId())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }

    @Override
    public boolean isUserInChatRoom(String userId, String chatRoomId) {
        return chatRoomUserRepository.existsByChatRoomIdAndUserId(chatRoomId, userId);
    }

    @Override
    public boolean isUsersInChatRoom(Set<String> userIds, String chatRoomId) {
        return false;
    }

    @Override
    public void reActivateChatRoom(String chatRoomId) {
        ChatRoom chatRoom = getChatRoomEntity(chatRoomId);
        Room room = roomService.getRoomEntityById(chatRoom.getRoomId());
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new APIException(HttpStatus.FORBIDDEN, ErrorCode.FLEXIBLE_ERROR, "Room is not available");
        }
        chatRoom.setStatus(ChatRoomStatus.ACTIVE);
        chatRoomRepository.save(chatRoom);
    }

    @Override
    public void testNotifyChatRoom() {
        ChatRoom chatRoom = ChatRoom.builder()
                .id("test")
                .name("Test")
                .type(ChatRoomType.GROUP)
                .status(ChatRoomStatus.ACTIVE)
                .build();
        ConversationResponse conversationResponse = ConversationResponse.builder()
                .chatRoomId(chatRoom.getId())
                .roomName(chatRoom.getName())
                .lastMessage(chatRoom.getLastMessage())
                .lastMessageTime(chatRoom.getLastMessageTimeStamp())
                .lastMessageSender(chatRoom.getLastMessageSender())
                .unreadCount(0)
                .isGroup(chatRoom.getType() == ChatRoomType.GROUP)
                .build();
        messagingTemplate.convertAndSendToUser("70f70be9-fd3a-4314-85c7-8e3881d8579a",
                                               "/queue/chat-room",
                                               conversationResponse);
    }

    private String generateDirectChatKey(String userId1, String userId2) {
        String[] ids = new String[]{userId1, userId2};
        Arrays.sort(ids);
        return "DIRECT_" + ids[0] + "_" + ids[1];
    }

    private String[] getUsersFromRoomId(String chatKey) {
        if (!chatKey.startsWith("DIRECT_")) {
            throw new IllegalArgumentException("Not a direct room ID");
        }
        String[] parts = chatKey.split("_");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid direct room ID format");
        }
        return new String[]{parts[1], parts[2]};
    }

    private void notifyNewChatRoom(ChatRoom chatRoom, String userId) {
        ConversationResponse conversationResponse = ConversationResponse.builder()
                .chatRoomId(chatRoom.getId())
                .roomName(chatRoom.getName())
                .lastMessage(chatRoom.getLastMessage())
                .lastMessageTime(chatRoom.getLastMessageTimeStamp())
                .lastMessageSender(chatRoom.getLastMessageSender())
                .unreadCount(0)
                .isGroup(chatRoom.getType() == ChatRoomType.GROUP)
                .build();
        messagingTemplate.convertAndSendToUser(userId, "/queue/chat-room", conversationResponse);
    }
}
