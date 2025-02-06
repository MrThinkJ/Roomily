package com.c2se.roomily.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "revenues")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Revenue {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "revenue_id")
    private String id;
    private BigDecimal amount;
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    @ManyToOne
    @JoinColumn(name = "landlord_id")
    private User landlord;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
