package com.c2se.roomily.repository;

import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.enums.ChatRoomStatus;
import com.c2se.roomily.enums.ChatRoomType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    ChatRoom findByFindPartnerPostIdAndType(String findPartnerPostId, ChatRoomType chatRoomType);
    Optional<ChatRoom> findByChatKey(String chatKey);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("UPDATE ChatRoom c SET c.status = 'ARCHIVED', c.requestId = NULL, c.findPartnerPostId = NULL WHERE c.findPartnerPostId = :findPartnerPostId")
    void archiveAllByFindPartnerPostId(String findPartnerPostId);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("UPDATE ChatRoom c SET c.status = :status WHERE c.id = :id")
    void updateStatusById(String id, ChatRoomStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Transactional
    @Query("SELECT c FROM ChatRoom c WHERE c.id = :id")
    Optional<ChatRoom> findByIdLocked(String id);

    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
            "JOIN ChatRoomUser cru1 ON cr.id = cru1.chatRoom.id " +
            "JOIN ChatRoomUser cru2 ON cr.id = cru2.chatRoom.id " +
            "WHERE cr.roomId = :roomId " +
            "AND cru1.user.id = :userId1 " +
            "AND cru2.user.id = :userId2")
    Optional<ChatRoom> findByRoomIdAndUsers(
            @Param("roomId") String roomId,
            @Param("userId1") String userId1,
            @Param("userId2") String userId2
    );

    Optional<ChatRoom> findByRentedRoomId(String rentedRoomId);

}
