package com.c2se.roomily.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutResponse {
    private String id;
    private String paymentLinkId;
    private String accountNumber;
    private String accountName;
    private Integer amount;
    private String description;
    private String checkoutUrl;
    private String qrCode;
    private Long orderCode;
    private String status;
    private String createdAt;
    private String expiresAt;
}
