package com.c2se.roomily.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_histories")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PricingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "pricing_history_id")
    private String id;
    private BigDecimal price;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
}
