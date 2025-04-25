package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.WithdrawInfoRequest;
import com.c2se.roomily.payload.response.WithdrawInfoResponse;

import java.math.BigDecimal;

public interface WalletService {
    WithdrawInfoResponse getWithdrawInfo(String userId);
    void updateWithdrawInfo(String userId, WithdrawInfoRequest request);
    void withdraw(String userId, String amount);
    void confirmWithdraw(String transactionId);
    void cancelWithdraw(String transactionId, String reason);
}
