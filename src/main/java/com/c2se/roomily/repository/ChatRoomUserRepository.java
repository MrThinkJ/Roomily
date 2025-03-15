package com.c2se.roomily.repository;

import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.entity.ChatRoomUser;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.payload.internal.ChatRoomUserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, String> {
    Optional<ChatRoomUser> findByChatRoomAndUser(ChatRoom chatRoom, User user);

    @Query("""
            SELECT
                new com.c2se.roomily.payload.internal.ChatRoomUserData(
                    cru.user.id,
                    cru.chatRoom.id,
                    cru.chatRoom.name,
                    cru.chatRoom.type,
                    cru.chatRoom.lastMessage,
                    cru.chatRoom.lastMessageTimeStamp,
                    cru.chatRoom.lastMessageSender,
                    cru.unreadMessageCount ,
                    cru.lastReadTimeStamp
                )
            FROM ChatRoomUser cru
            WHERE cru.user.id = :userId ORDER BY cru.chatRoom.lastMessageTimeStamp DESC
            """)
    List<ChatRoomUserData> findDataByUserId(String userId);

    @Query("SELECT cru.user.id FROM ChatRoomUser cru WHERE cru.chatRoom.id = :chatRoomId")
    List<String> findUserIdInChatRoomByChatRoomId(String chatRoomId);

    boolean existsByChatRoomIdAndUserId(String chatRoomId, String userId);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("DELETE FROM ChatRoomUser cru WHERE cru.chatRoom.id = :chatRoomId")
    void deleteByChatRoomId(String chatRoomId);
}
