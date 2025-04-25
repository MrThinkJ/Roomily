package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WithdrawInfoResponse {
    private String id;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private String lastWithdrawDate;
    private String userId;
}
