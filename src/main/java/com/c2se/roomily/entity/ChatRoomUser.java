package com.c2se.roomily.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room_users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "chat_room_user_id")
    private String id;
    @Column(name = "unread_message_count")
    private Integer unreadMessageCount;
    @Column(name = "last_read_timestamp")
    private LocalDateTime lastReadTimeStamp;
    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
