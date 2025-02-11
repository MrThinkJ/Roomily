package com.c2se.roomily.payload.internal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayOsTransactionDto {
    private String reference;
    private Integer amount;
    private String accountNumber;
    private String description;
    private String transactionDateTime;
}
