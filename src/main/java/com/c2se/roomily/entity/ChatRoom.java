package com.c2se.roomily.entity;

import com.c2se.roomily.enums.BaseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "chat_room_id")
    private String id;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    private BaseStatus status;
    @ManyToOne
    @JoinColumn(name = "user1_id")
    private User user1;
    @ManyToOne
    @JoinColumn(name = "user2_id")
    private User user2;
}
