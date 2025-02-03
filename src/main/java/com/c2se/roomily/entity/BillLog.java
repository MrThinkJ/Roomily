package com.c2se.roomily.entity;

import com.c2se.roomily.enums.BillStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bill_logs")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class BillLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "bill_log_id")
    private String id;
    @Column(name = "from_date")
    private LocalDateTime fromDate;
    @Column(name = "to_date")
    private LocalDateTime toDate;
    private Double electricity;
    private Double water;
    @Column(name = "electricity_bill")
    private BigDecimal electricityBill;
    @Column(name = "water_bill")
    private BigDecimal waterBill;
    @Column(name = "status")
    private BillStatus billStatus;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
}
