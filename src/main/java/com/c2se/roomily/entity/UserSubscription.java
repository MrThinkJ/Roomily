package com.c2se.roomily.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_subscription", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "subscription_id"})
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_subscription_id")
    private String id;
    @Column(name = "start_date")
    private LocalDateTime startDate;
    @Column(name = "end_date")
    private LocalDateTime endDate;
    @Column(name = "auto_renew")
    private boolean autoRenew;
    @Column(name = "remaining_credits")
    private Integer remainingCredits;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;
}
