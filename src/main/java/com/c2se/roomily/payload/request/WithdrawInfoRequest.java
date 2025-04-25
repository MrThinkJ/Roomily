package com.c2se.roomily.payload.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WithdrawInfoRequest {
    @NotNull(message = "Bank name is required")
    @NotEmpty(message = "Bank name is required")
    private String bankName;
    @NotNull(message = "Account number is required")
    @NotEmpty(message = "Account number is required")
    private String accountNumber;
    @NotNull(message = "Account name is required")
    @NotEmpty(message = "Account name is required")
    private String accountName;
}
