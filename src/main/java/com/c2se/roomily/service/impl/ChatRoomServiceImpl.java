package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.entity.ChatRoomUser;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ChatRoomType;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    public ChatRoom getChatRoomEntity(String roomId) {
        return chatRoomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("ChatRoom", "Id", roomId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createGroupChatRoom(String managerId, Set<String> userIds, String chatRoomName, String roomId) {
        userIds.add(managerId);
        Set<User> users = userService.getUserEntities(List.copyOf(userIds));
        if (users.size() != userIds.size()) {
            throw new IllegalArgumentException("Invalid user ids");
        }
        if (!chatRoomRepository.existsById(roomId)) {
            throw new IllegalArgumentException("Invalid room id");
        }
        ChatRoom chatRoom = ChatRoom.builder()
                .name(chatRoomName)
                .managerId(managerId)
                .type(ChatRoomType.GROUP)
                .roomId(roomId)
                .build();
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        List<ChatRoomUser> chatRoomUsers = users.stream().map(user -> ChatRoomUser.builder()
                .chatRoom(savedChatRoom)
                .user(user)
                .build()).toList();
        chatRoomUserRepository.saveAll(chatRoomUsers);
        users.forEach(user -> notifyNewChatRoom(savedChatRoom, user.getId()));
        return savedChatRoom.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatRoom getOrCreateDirectChatRoom(String userId1, String userId2, String roomId) {
        String chatRoomId = generateChatRoomId(userId1, userId2);
        Optional<ChatRoom> existChatRoom = chatRoomRepository.findById(roomId);
        if (existChatRoom.isPresent())
            return existChatRoom.get();
        Set<User> users = userService.getUserEntities(List.of(userId1, userId2));
        if (users.size() != 2)
            throw new IllegalArgumentException("Invalid user ids");
        if (!chatRoomRepository.existsById(chatRoomId)) {
            throw new IllegalArgumentException("Invalid room id");
        }
        ChatRoom chatRoom = ChatRoom.builder()
                .id(chatRoomId)
                .name("DM_" + userId1 + "_" + userId2)
                .type(ChatRoomType.DIRECT)
                .roomId(roomId)
                .build();
        chatRoomRepository.save(chatRoom);
        List<ChatRoomUser> chatRoomUsers = users.stream().map(user -> ChatRoomUser.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build()).toList();
        chatRoomUserRepository.saveAll(chatRoomUsers);
        users.forEach(user -> notifyNewChatRoom(chatRoom, user.getId()));
        return chatRoom;
    }

    @Override
    public void addUserToGroupChatRoom(String roomId, String userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("ChatRoom", "Id", roomId));
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
    public void removeUserFromGroupChatRoom(String managerId, String roomId, String userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("ChatRoom", "Id", roomId));
        if (chatRoom.getType() != ChatRoomType.GROUP) {
            throw new IllegalArgumentException("Invalid chat room type");
        }
        if (!chatRoom.getManagerId().equals(managerId))
            throw new IllegalArgumentException("Only manager can remove user from group chat room");
        User user = userService.getUserEntity(userId);
        ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomAndUser(chatRoom, user).orElseThrow(
                () -> new ResourceNotFoundException("ChatRoom", "ChatRoom Or User", roomId + " " + userId)
        );
        chatRoomUserRepository.delete(chatRoomUser);
    }

    @Override
    public void exitGroupChatRoom(String roomId, String userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("ChatRoom", "Id", roomId));
        if (chatRoom.getType() != ChatRoomType.GROUP) {
            throw new IllegalArgumentException("Invalid chat room type");
        }
        User user = userService.getUserEntity(userId);
        ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomAndUser(chatRoom, user).orElseThrow(
                () -> new ResourceNotFoundException("ChatRoom", "ChatRoom Or User", roomId + " " + userId)
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
    public String getChatRoomIdByFindPartnerPostId(String findPartnerPostId) {
        return chatRoomRepository.findByFindPartnerPostId(findPartnerPostId).orElseThrow(
                () -> new ResourceNotFoundException("ChatRoom", "FindPartnerPostId", findPartnerPostId)
        ).getId();
    }

    @Override
    public List<User> getChatRoomUsers(String roomId) {
        // TODO: After implement user service, implement this method, return user list of chat room, can return ids only
        return null;
    }

    @Override
    public List<String> getChatRoomUserIds(String roomId) {
        getChatRoomEntity(roomId);
        return chatRoomUserRepository.findUserIdInChatRoomByChatRoomId(roomId);
    }

    @Override
    public List<ConversationResponse> getChatRoomsByUserId(String userId) {
        List<ChatRoomUserData> chatRoomUserData = chatRoomUserRepository.findDataByUserId(userId);
        if (chatRoomUserData.isEmpty()) {
            return List.of();
        }
        return chatRoomUserData.stream().map(data -> ConversationResponse.builder()
                .chatRoomId(data.roomId())
                .roomName(data.roomName())
                .lastMessage(data.lastMessage())
                .lastMessageTime(data.lastMessageTimeStamp())
                .lastMessageSender(data.lastMessageSender())
                .unreadCount(data.unreadCount())
                .isGroup(data.type() == ChatRoomType.GROUP)
                .build()).collect(Collectors.toList());
    }

    @Override
    public ChatRoomResponse getChatRoomInfo(String roomId) {
        ChatRoom chatRoom = getChatRoomEntity(roomId);
        return ChatRoomResponse.builder()
                .chatRoomId(chatRoom.getId())
                .roomName(chatRoom.getName())
                .build();
    }

    @Override
    public boolean isUserInChatRoom(String userId, String roomId) {
        return chatRoomUserRepository.existsByChatRoomIdAndUserId(roomId, userId);
    }

    @Override
    public boolean isUsersInChatRoom(Set<String> userIds, String roomId) {
        return false;
    }

    private String generateChatRoomId(String userId1, String userId2) {
        String[] ids = {userId1, userId2};
        Arrays.sort(ids);
        return "private_" + ids[0] + "_" + ids[1];
    }

    private String[] getUsersFromRoomId(String roomId) {
        if (!roomId.startsWith("private_")) {
            throw new IllegalArgumentException("Not a direct room ID");
        }
        String[] parts = roomId.split("_");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid direct room ID format");
        }
        return new String[]{parts[1], parts[2]};
    }

    private void notifyNewChatRoom(ChatRoom chatRoom, String userId){
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
