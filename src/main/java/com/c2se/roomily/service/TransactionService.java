package com.c2se.roomily.service;

import com.c2se.roomily.entity.Transaction;
import com.c2se.roomily.payload.response.TransactionPageResponse;
import com.c2se.roomily.payload.response.TransactionResponse;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    Transaction getTransactionEntityById(String id);
    TransactionPageResponse getAllTransactions(int page, int size,
                                               String sortBy, String sortDir);

    TransactionResponse getTransactionById(String id);

    List<TransactionResponse> getTransactionTopUpToRentedRoomWallet(String rentedRoomId);
    void saveTransaction(Transaction transaction);
    void deleteTransaction(String id);

    TransactionPageResponse getTransactionsByUserId(String userId, int page, int size,
                                                    String sortBy, String sortDir);

    TransactionPageResponse getTransactionsByStatus(String status, int page, int size,
                                                    String sortBy, String sortDir);

    TransactionPageResponse getTransactionsByTypeAndStatus(String type, String status, int page, int size,
                                                           String sortBy, String sortDir);
                                                           
    // New methods for admin dashboard
    BigDecimal getTotalCompletedWithdrawalsThisMonth();
    
    BigDecimal getTotalCompletedDepositsThisMonth();
    
    long getPendingWithdrawalsCount();
    
    long getCompletedTransactionsCount();
    
    BigDecimal getTotalTransactionVolume();
}
