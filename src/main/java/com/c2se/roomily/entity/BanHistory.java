package com.c2se.roomily.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ban_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BanHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String reason;
    private LocalDateTime bannedAt;
    private LocalDateTime expiresAt;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
} 