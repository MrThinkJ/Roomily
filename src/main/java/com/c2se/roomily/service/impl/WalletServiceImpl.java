package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.Transaction;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.entity.WithdrawInfo;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.enums.TransactionStatus;
import com.c2se.roomily.enums.TransactionType;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.WithdrawInfoRequest;
import com.c2se.roomily.payload.response.WithdrawInfoResponse;
import com.c2se.roomily.repository.WithdrawInfoRepository;
import com.c2se.roomily.service.TransactionService;
import com.c2se.roomily.service.UserService;
import com.c2se.roomily.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {
    private final TransactionService transactionService;
    private final WithdrawInfoRepository withdrawInfoRepository;
    private final UserService userService;

    @Override
    public WithdrawInfoResponse getWithdrawInfo(String userId) {
        WithdrawInfo withdrawInfo = withdrawInfoRepository.findByUserId(userId).orElse(null);
        if (withdrawInfo == null) {
            return null;
        }
        return mapToWithdrawInfoResponse(withdrawInfo);
    }

    @Override
    public void updateWithdrawInfo(String userId, WithdrawInfoRequest request) {
        WithdrawInfo withdrawInfo = withdrawInfoRepository.findByUserId(userId).orElse(
                WithdrawInfo.builder()
                        .user(userService.getUserEntityById(userId))
                        .build());
        withdrawInfo.setBankName(request.getBankName());
        withdrawInfo.setAccountNumber(request.getAccountNumber());
        withdrawInfo.setAccountName(request.getAccountName());
        withdrawInfoRepository.save(withdrawInfo);
    }

    @Override
    public void withdraw(String userId, String amount) {
        BigDecimal withdrawAmount = new BigDecimal(amount);
        User user = userService.getUserEntityById(userId);
        if (user.getBalance().compareTo(withdrawAmount) < 0){
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.INSUFFICIENT_BALANCE, user.getBalance());
        }
        Transaction transaction = Transaction.builder()
                .user(user)
                .amount(withdrawAmount)
                .status(TransactionStatus.PENDING)
                .type(TransactionType.WITHDRAWAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        transactionService.saveTransaction(transaction);
        user.setBalance(user.getBalance().subtract(withdrawAmount));
        userService.saveUser(user);
        log.info("Transaction {} is created", transaction.getId());
        log.info("User {} balance is updated to {}", user.getId(), user.getBalance());
        WithdrawInfo withdrawInfo = withdrawInfoRepository.findByUserId(userId).orElseThrow(
                () -> new ResourceNotFoundException("WithdrawInfo", "userId", userId));
        withdrawInfo.setLastWithdrawDate(LocalDate.now());
        withdrawInfoRepository.save(withdrawInfo);
        log.info("WithdrawInfo {} is updated", withdrawInfo.getId());

    }

    @Override
    public void confirmWithdraw(String transactionId) {
        Transaction transaction = transactionService.getTransactionEntityById(transactionId);
        if (transaction.getStatus() != TransactionStatus.PENDING){
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.TRANSACTION_STATUS, transaction.getStatus());
        }
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionService.saveTransaction(transaction);
        log.info("Transaction {} is confirmed", transactionId);
    }

    @Override
    public void cancelWithdraw(String transactionId, String reason) {
        Transaction transaction = transactionService.getTransactionEntityById(transactionId);
        if (transaction.getStatus() != TransactionStatus.PENDING){
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.TRANSACTION_STATUS, transaction.getStatus());
        }
        transaction.setStatus(TransactionStatus.CANCELLED);
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setMetadata(reason);
        transactionService.saveTransaction(transaction);
        log.info("Transaction {} is cancelled", transactionId);
        User user = transaction.getUser();
        user.setBalance(user.getBalance().add(transaction.getAmount()));
        userService.saveUser(user);
        log.info("User {} balance is updated to {}", user.getId(), user.getBalance());
    }

    private WithdrawInfoResponse mapToWithdrawInfoResponse(WithdrawInfo withdrawInfo) {
        return WithdrawInfoResponse.builder()
                .id(withdrawInfo.getId())
                .userId(withdrawInfo.getUser().getId())
                .bankName(withdrawInfo.getBankName())
                .accountNumber(withdrawInfo.getAccountNumber())
                .accountName(withdrawInfo.getAccountName())
                .lastWithdrawDate(withdrawInfo.getLastWithdrawDate() != null ?
                                          withdrawInfo.getLastWithdrawDate().toString() : null)
                .build();
    }
}
