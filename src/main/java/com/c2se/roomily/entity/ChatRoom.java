package com.c2se.roomily.entity;

import com.c2se.roomily.enums.ChatRoomStatus;
import com.c2se.roomily.enums.ChatRoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "chat_room_id")
    private String id;
    @Column(name = "chat_key", unique = true)
    private String chatKey;
    private String name;
    @Column(name = "manager_id")
    private String managerId;
    @Column(name = "next_sub_id")
    private Integer nextSubId = 1;
    @Enumerated(EnumType.STRING)
    private ChatRoomType type;
    @Enumerated(EnumType.STRING)
    private ChatRoomStatus status;
    @Column(name = "last_message")
    private String lastMessage;
    @Column(name = "last_message_timestamp")
    private LocalDateTime lastMessageTimeStamp;
    @Column(name = "last_message_sender")
    private String lastMessageSender;
    @Column(name = "room_id")
    private String roomId;
    @Column(name = "find_partner_post_id")
    private String findPartnerPostId;
    @Column(name = "request_id")
    private String requestId;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "rented_room_id")
    private String rentedRoomId;
}
