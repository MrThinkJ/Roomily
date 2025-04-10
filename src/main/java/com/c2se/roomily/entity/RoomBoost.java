package com.c2se.roomily.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_boosts")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomBoost {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "boost_id")
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "credits_used")
    private Integer creditsUsed;
    
    @Column(name = "is_active")
    private boolean active;
    
    @Column(name = "boost_level")
    private Integer boostLevel;
    
    @Column(name = "radius_km")
    private Double radiusKm;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
