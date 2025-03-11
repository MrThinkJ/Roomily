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
import java.time.LocalDate;
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
    private LocalDate fromDate;
    private LocalDate toDate;
    private Double electricity;
    private Double water;
    private BigDecimal electricityBill;
    private BigDecimal waterBill;
    private BigDecimal rentalCost;
    private String roomId;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BillStatus billStatus;
    private String waterImage;
    private String electricityImage;
    private String landlordComment;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "rented_room_id")
    private RentedRoom rentedRoom;
}
