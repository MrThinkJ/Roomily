package com.c2se.roomily.entity;

import com.c2se.roomily.enums.PromotedRoomStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promoted_rooms", indexes = {
        @Index(name = "idx_promoted_room_ad_campaign_id", columnList = "ad_campaign_id"),
        @Index(name = "idx_promoted_room_room_id", columnList = "room_id")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PromotedRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "promoted_room_id")
    private String id;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PromotedRoomStatus status;
    @Column(name = "cpc_bid", precision = 10, scale = 2, nullable = false)
    private BigDecimal cpcBid;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_campaign_id", nullable = false)
    private AdCampaign adCampaign;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
}
