package com.c2se.roomily.payload.internal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateRentDepositPaymentLinkRequest {
    private String productName;
    private String description;
    private String rentedRoomId;
    private int amount;
    private String checkoutId;
    private String chatMessageId;
}
