package com.c2se.roomily.entity;

import com.c2se.roomily.enums.RentedRoomStatus;
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
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rented_rooms")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RentedRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rented_room_id")
    private String id;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(name = "rental_deposit")
    private BigDecimal rentalDeposit;
    @Column(name = "debt_date")
    private LocalDate debtDate;
    @Column(name = "rented_room_wallet")
    private BigDecimal rentedRoomWallet;
    @Column(name = "wallet_debt")
    private BigDecimal walletDebt;
    @Enumerated(EnumType.STRING)
    private RentedRoomStatus status;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    @ManyToOne
    @JoinColumn(name = "landlord_id")
    private User landlord;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rented_room_co_tenants",
            joinColumns = @JoinColumn(name = "rented_room_id"),
            inverseJoinColumns = @JoinColumn(name = "co_tenant_id")
    )
    private Set<User> coTenants = new HashSet<>();
}
