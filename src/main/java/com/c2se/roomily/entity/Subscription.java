package com.c2se.roomily.entity;

import com.c2se.roomily.enums.SubscriptionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "subscriptions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "subscription_id")
    private String id;
    @Column(name = "subscription_type")
    private SubscriptionType type;
    @Column(name = "subscription_price")
    private BigDecimal price;
}
