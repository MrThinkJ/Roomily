package com.c2se.roomily.entity;

import com.c2se.roomily.enums.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_histories")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PricingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "pricing_history_id")
    private String id;
    
    private String city;
    private String district;
    private String ward;
    
    private BigDecimal averagePrice;
    private BigDecimal medianPrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    
    @Enumerated(EnumType.STRING)
    private RoomType roomType;
    
    @Column(name = "min_square_meters")
    private Double minSquareMeters;
    @Column(name = "max_square_meters")
    private Double maxSquareMeters;
    
    @Column(name = "period_start")
    private LocalDateTime periodStart;
    @Column(name = "period_end")
    private LocalDateTime periodEnd;
    
    @Column(name = "sample_size")
    private Integer sampleSize;
}
