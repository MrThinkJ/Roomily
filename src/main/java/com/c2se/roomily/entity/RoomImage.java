package com.c2se.roomily.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "room_image_id")
    private String id;
    private String url;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;
}
