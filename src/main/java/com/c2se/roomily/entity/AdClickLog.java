package com.c2se.roomily.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ad_click_logs")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdClickLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ad_click_log_id")
    private String id;
    private String campaignId;
    private String promotedRoomId;
    private String userId;
    private LocalDateTime timestamp;
    private BigDecimal cost;
    private String ipAddress;
}
