package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePaymentLinkRequest {
    //    @NotBlank(message = "Product name is required")
    private String productName;
    //    @NotBlank(message = "Description is required")
    private String description;
    //    @NotNull(message = "Price is required")
//    @Min(value = 0, message = "Price must be a positive number")
    private boolean isInAppWallet;
    private String rentedRoomId;
    private int price;
}
