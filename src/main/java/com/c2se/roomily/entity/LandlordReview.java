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
@Table(name = "landlord_reviews")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LandlordReview {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "landlord_review_id")
    private String id;
    private int rating;
    private String content;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @ManyToOne
    @JoinColumn(name = "landlord_id")
    private User landlord;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User reviewer;
}
