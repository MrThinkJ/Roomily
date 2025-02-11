package com.c2se.roomily.repository;

import com.c2se.roomily.entity.Transaction;
import com.c2se.roomily.enums.TransactionStatus;
import com.c2se.roomily.enums.TransactionType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    @NotNull
    Page<Transaction> findAll(@NotNull Pageable pageable);
    Page<Transaction> findByUserId(String userId, Pageable pageable);
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);
    Page<Transaction> findByTypeAndStatus(TransactionType type, TransactionStatus status, Pageable pageable);
    Transaction findByPaymentId(String paymentId);
} 