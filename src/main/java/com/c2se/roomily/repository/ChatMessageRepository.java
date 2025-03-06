package com.c2se.roomily.repository;

import com.c2se.roomily.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    @Query(value = "SELECT * FROM chat_messages WHERE (created_at, sub_id) < (?3, ?2) AND room_id = ?1 " +
            "ORDER BY created_at DESC LIMIT ?4", nativeQuery = true)
    List<ChatMessage> findByRoomId(String roomId, String pivot, String timestamp, int prev);

    @Query(value = "SELECT * FROM chat_messages WHERE room_id = ?1 ORDER BY created_at DESC LIMIT ?2", nativeQuery = true)
    List<ChatMessage> findLastedByRoomId(String roomId, int prev);
} 