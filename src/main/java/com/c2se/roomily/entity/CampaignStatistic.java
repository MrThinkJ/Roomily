package com.c2se.roomily.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaign_statistics")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignStatistic {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "campaign_statistic_id")
    private String id;
    @Column(name = "clicks", nullable = false)
    private long clicks = 0L;
    @Column(name = "conversions", nullable = false)
    private long conversionCount = 0L;
    @Column(name = "impressions", nullable = false)
    private long impressionCount = 0L;
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_campaign_id", nullable = false)
    private AdCampaign adCampaign;
}
