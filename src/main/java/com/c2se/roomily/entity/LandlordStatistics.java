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
@Table(name = "landlord_statistics")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LandlordStatistics {
    @Id
    @Column(name = "landlord_id")
    private String landlordId;
    private Double responseRate;
    private Long totalChatRooms;
    private Long respondedChatRooms;
    private Long averageResponseTimeMinutes;
    private Integer totalRentedRooms;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 