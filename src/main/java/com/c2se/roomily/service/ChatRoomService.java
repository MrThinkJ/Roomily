package com.c2se.roomily.service;

import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.payload.response.ChatRoomResponse;
import com.c2se.roomily.payload.response.ConversationResponse;

import java.util.List;
import java.util.Set;

public interface ChatRoomService {
    ChatRoom getChatRoomEntity(String roomId);
    void updateChatRoomStatus(String roomId, String status);
    void archiveAllChatRoomsByFindPartnerPostId(String findPartnerPostId);
    String createGroupChatRoom(String managerId, Set<String> userIds, String chatRoomName, String roomId);
    ChatRoom getOrCreateDirectChatRoom(String userId1, String userId2, String findPartnerPostId);
    void addUserToGroupChatRoom(String roomId, String userId);
    void removeUserFromGroupChatRoom(String managerId, String roomId, String userId);
    void exitGroupChatRoom(String roomId, String userId);
    void deleteGroupChatRoom(String managerId, String roomId);
    String getChatRoomIdByFindPartnerPostId(String findPartnerPostId);
    List<User> getChatRoomUsers(String roomId);
    List<String> getChatRoomUserIds(String roomId);
    List<ConversationResponse> getChatRoomsByUserId(String userId);
    ChatRoomResponse getChatRoomInfo(String roomId);
    boolean isUserInChatRoom(String userId, String roomId);
    boolean isUsersInChatRoom(Set<String> userIds, String roomId);
    void testNotifyChatRoom();
}
