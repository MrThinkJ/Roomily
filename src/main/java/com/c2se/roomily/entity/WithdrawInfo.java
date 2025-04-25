package com.c2se.roomily.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "withdraw_infos")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "info_id")
    private String id;
    @Column(name = "bank_name")
    private String bankName;
    @Column(name = "account_number")
    private String accountNumber;
    @Column(name = "account_name")
    private String accountName;
    @Column(name = "last_withdraw")
    private LocalDate lastWithdrawDate;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
