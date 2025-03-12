package com.c2se.roomily.repository;

import com.c2se.roomily.entity.ChatRoom;
import com.c2se.roomily.enums.ChatRoomStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    Optional<ChatRoom> findByFindPartnerPostId(String findPartnerPostId);

    Optional<ChatRoom> findByChatKey(String chatKey);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("UPDATE ChatRoom c SET c.status = 'ARCHIVED' WHERE c.findPartnerPostId = :findPartnerPostId")
    void archiveAllByFindPartnerPostId(String findPartnerPostId);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("UPDATE ChatRoom c SET c.status = :status WHERE c.id = :id")
    void updateStatusById(String id, ChatRoomStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ChatRoom c WHERE c.id = :id")
    Optional<ChatRoom> findByIdLocked(String id);

}
