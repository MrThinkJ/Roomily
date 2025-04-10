package com.c2se.roomily.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_statistics")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;
    
    @Column(name = "tenant_id", unique = true)
    private String tenantId;
    
    @Column(name = "success_rented_rate")
    private Double successRentedRate;
    
    @Column(name = "debt_rate")
    private Double debtRate;
    
    @Column(name = "total_rented_rooms")
    private Integer totalRentedRooms;
    
    @Column(name = "total_success_rented")
    private Integer totalSuccessRented;
    
    @Column(name = "total_late_payments")
    private Integer totalLatePayments;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 