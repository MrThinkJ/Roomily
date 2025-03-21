package com.c2se.roomily.service;

import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ChatRoomStatus;
import com.c2se.roomily.enums.ChatRoomType;
import com.c2se.roomily.payload.response.ChatRoomResponse;
import com.c2se.roomily.payload.response.ConversationResponse;

import java.util.List;
import java.util.Set;

public interface ChatRoomService {
    ChatRoom getChatRoomEntity(String chatRoomId);
    void saveChatRoom(ChatRoom chatRoom);
    void updateChatRoomStatus(String chatRoomId, ChatRoomStatus chatRoomStatus);

    void archiveAllChatRoomsByFindPartnerPostId(String findPartnerPostId);

    ChatRoomResponse createGroupChatRoom(String managerId, Set<String> userIds, String chatRoomName, String roomId);

    ChatRoomResponse getOrCreateDirectChatRoom(String userId1, String userId2, String findPartnerPostId);

    ChatRoomResponse createDirectChatRoomToLandlord(String userId, String roomId);

    void addUserToGroupChatRoom(String chatRoomId, String userId);

    void removeUserFromGroupChatRoom(String managerId, String chatRoomId, String userId);

    void exitGroupChatRoom(String chatRoomId, String userId);

    void deleteGroupChatRoom(String managerId, String chatRoomId);

    ChatRoom getChatRoomByFindPartnerPostIdAndType(String findPartnerPostId, ChatRoomType chatRoomType);
    ChatRoom getDirectChatRoomByUserIds(String userId1, String userId2);
    List<User> getChatRoomUsers(String chatRoomId);

    List<String> getChatRoomUserIds(String chatRoomId);

    List<ConversationResponse> getChatRoomsByUserId(String userId);

    ChatRoomResponse getChatRoomInfo(String chatRoomId);

    boolean isUserInChatRoom(String userId, String chatRoomId);

    boolean isUsersInChatRoom(Set<String> userIds, String chatRoomId);

    void reActivateChatRoom(String chatRoomId);

    void testNotifyChatRoom();
}
