package com.c2se.roomily.payload.response;

import com.c2se.roomily.payload.internal.PayOsTransactionDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PaymentLinkResponse {
    private String id;
    private Long orderCode;
    private Integer amount;
    private Integer amountPaid;
    private Integer amountRemaining;
    private String status;
    private String createdAt;
    private List<PayOsTransactionDto> transactions;
    private String cancellationReason;
    private String canceledAt;
}
