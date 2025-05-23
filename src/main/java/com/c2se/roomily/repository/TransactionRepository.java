package com.c2se.roomily.repository;

import com.c2se.roomily.entity.Transaction;
import com.c2se.roomily.enums.TransactionStatus;
import com.c2se.roomily.enums.TransactionType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    @NotNull
    Page<Transaction> findAll(@NotNull Pageable pageable);

    Page<Transaction> findByUserId(String userId, Pageable pageable);

    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);

    Page<Transaction> findByTypeAndStatus(TransactionType type, TransactionStatus status, Pageable pageable);

    List<Transaction> findByMetadataAndType(String metadata, TransactionType type);

    Transaction findByPaymentId(String paymentId);

    List<Transaction> findByTypeAndStatusAndCreatedAtBetween(
            TransactionType type, 
            TransactionStatus status, 
            LocalDateTime start, 
            LocalDateTime end);

    List<Transaction> findByStatusAndCreatedAtBetween(
            TransactionStatus status,
            LocalDateTime start,
            LocalDateTime end);

    List<Transaction> findByStatus(TransactionStatus status);

    long countByTypeAndStatus(TransactionType type, TransactionStatus status);

    long countByStatus(TransactionStatus status);
} 