package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutResponse {
    private String accountNumber;
    private String accountName;
    private Integer amount;
    private String description;
    private String checkoutUrl;
    private String qrCode;
    private Long orderCode;
    private String status;
}
