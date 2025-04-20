package com.c2se.roomily.entity;

import com.c2se.roomily.enums.AdCampaignStatus;
import com.c2se.roomily.enums.PricingModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ad_campaigns")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ad_campaign_id")
    private String id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "pricing_model", nullable = false)
    private PricingModel pricingModel;
    @Column(name = "cpm_rate", precision = 10, scale = 2)
    private BigDecimal cpmRate;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AdCampaignStatus status;
    @Column(name = "budget", precision = 10, scale = 2, nullable = false)
    private BigDecimal budget = BigDecimal.ZERO;
    @Column(name = "spent_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal spentAmount = BigDecimal.ZERO;
    @Column(name = "daily_budget", precision = 10, scale = 2, nullable = false)
    private BigDecimal dailyBudget;
    @Column(name = "daily_spent_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal dailySpentAmount = BigDecimal.ZERO;
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    @Column(name = "end_date")
    private LocalDateTime endDate;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @OneToMany(mappedBy = "adCampaign", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromotedRoom> promotedRooms;
    @OneToMany(mappedBy = "adCampaign", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CampaignStatistic> campaignStatistics;
}
