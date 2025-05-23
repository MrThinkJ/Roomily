package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.Transaction;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.enums.TransactionStatus;
import com.c2se.roomily.enums.TransactionType;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.response.PageResponse;
import com.c2se.roomily.payload.response.TransactionPageResponse;
import com.c2se.roomily.payload.response.TransactionResponse;
import com.c2se.roomily.repository.TransactionRepository;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.service.TransactionService;
import com.c2se.roomily.util.AppConstants;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    TransactionRepository transactionRepository;
    UserRepository userRepository;

    @Override
    public Transaction getTransactionEntityById(String id) {
        return transactionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Transaction", "id", id)
        );
    }

    @Override
    public TransactionPageResponse getAllTransactions(int page, int size,
                                                      String sortBy, String sortDir) {
        Sort sort = Sort.by(
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Transaction> transactions = transactionRepository.findAll(pageable);
        return mapToPageResponse(transactions);
    }

    @Override
    public TransactionResponse getTransactionById(String id) {
        return mapToResponse(transactionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Transaction", "id", id)
        ));
    }

    @Override
    public List<TransactionResponse> getTransactionTopUpToRentedRoomWallet(String rentedRoomId) {
        List<Transaction> transactions = transactionRepository.findByMetadataAndType(
                rentedRoomId, TransactionType.RENT_PAYMENT);
        return transactions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public void saveTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    @Override
    public void deleteTransaction(String id) {
        transactionRepository.deleteById(id);
    }

    @Override
    public TransactionPageResponse getTransactionsByUserId(String userId, int page, int size,
                                                           String sortBy, String sortDir) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", userId)
        );
        Sort sort = Sort.by(
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Transaction> transactions = transactionRepository.findByUserId(user.getId(), pageable);
        return mapToPageResponse(transactions);
    }

    @Override
    public TransactionPageResponse getTransactionsByStatus(String status, int page, int size,
                                                           String sortBy, String sortDir) {
        Sort sort = Sort.by(
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        TransactionStatus transactionStatus = TransactionStatus.valueOf(status);
        Page<Transaction> transactions = transactionRepository.findByStatus(transactionStatus, pageable);

        return mapToPageResponse(transactions);
    }

    @Override
    public TransactionPageResponse getTransactionsByTypeAndStatus(String type, String status,
                                                                  int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        TransactionType transactionType = TransactionType.valueOf(type);
        TransactionStatus transactionStatus = TransactionStatus.valueOf(status);
        Page<Transaction> transactions = transactionRepository.findByTypeAndStatus(
                transactionType, transactionStatus, pageable);
        return mapToPageResponse(transactions);
    }

    @Override
    public BigDecimal getTotalCompletedWithdrawalsThisMonth() {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        List<Transaction> withdrawals = transactionRepository.findByTypeAndStatusAndCreatedAtBetween(
                TransactionType.WITHDRAWAL, 
                TransactionStatus.COMPLETED,
                startOfMonth,
                endOfMonth);
                
        return withdrawals.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public BigDecimal getTotalCompletedDepositsThisMonth() {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        List<Transaction> deposits = transactionRepository.findByTypeAndStatusAndCreatedAtBetween(
                TransactionType.DEPOSIT, 
                TransactionStatus.COMPLETED,
                startOfMonth,
                endOfMonth);
                
        return deposits.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public long getPendingWithdrawalsCount() {
        return transactionRepository.countByTypeAndStatus(
                TransactionType.WITHDRAWAL, 
                TransactionStatus.PENDING);
    }
    
    @Override
    public long getCompletedTransactionsCount() {
        return transactionRepository.countByStatus(TransactionStatus.COMPLETED);
    }
    
    @Override
    public BigDecimal getTotalTransactionVolume() {
        List<Transaction> completedTransactions = transactionRepository.findByStatus(TransactionStatus.COMPLETED);
        return completedTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount().toString())
                .status(transaction.getStatus().name())
                .type(transaction.getType().name())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .userId(transaction.getUser().getId())
                .userName(transaction.getUser().getUsername())
                .checkoutResponseId(transaction.getCheckoutResponseId())
                .build();
    }

    private TransactionPageResponse mapToPageResponse(Page<Transaction> transactions) {
        PageResponse pageResponse = PageResponse.builder()
                .totalPages(transactions.getTotalPages())
                .totalElements(transactions.getTotalElements())
                .currentPage(transactions.getNumber())
                .hasNext(!transactions.isLast())
                .hasPrevious(!transactions.isFirst())
                .build();
        return TransactionPageResponse.builder()
                .content(transactions.stream().map(this::mapToResponse).collect(Collectors.toList()))
                .page(pageResponse)
                .build();
    }
}
