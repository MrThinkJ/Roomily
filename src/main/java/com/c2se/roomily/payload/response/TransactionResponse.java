package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionResponse {
    private String id;
    private String amount;
    private String status;
    private String type;
    private String createdAt;
    private String updatedAt;
    private String userId;
    private String userName;
}
