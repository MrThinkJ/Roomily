package com.c2se.roomily.repository;

import com.c2se.roomily.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    @Query(value = "SELECT * FROM chat_messages WHERE (created_at, sub_id) < (?3, ?2) AND chat_room_id = ?1 " +
            "ORDER BY created_at DESC LIMIT ?4", nativeQuery = true)
    List<ChatMessage> findByRoomId(String roomId, String pivot, String timestamp, int prev);

    @Query(value = "SELECT * FROM chat_messages WHERE chat_room_id = ?1 ORDER BY created_at DESC LIMIT ?2", nativeQuery = true)
    List<ChatMessage> findLastedByRoomId(String roomId, int prev);

    @Query(value = "SELECT cm FROM ChatMessage cm JOIN ChatRoom cr ON cm.chatRoom.id = cr.id " +
            "WHERE cr.id = :chatRoomId and cm.createdAt > :createdAt ORDER BY cm.createdAt DESC")
    List<ChatMessage> findByChatRoomId(String chatRoomId, LocalDateTime createdAt);
} 