package com.c2se.roomily.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ErrorCode {
    // Basic errors (1000-1099)
    RESOURCE_NOT_FOUND(1000, "%s with id %s not found"),
    RESOURCE_ALREADY_EXISTS(1001, "%s with id %s already exists"),
    RESOURCE_NOT_AVAILABLE(1002, "%s with id %s is not available"),
    RESOURCE_NOT_ACTIVE(1003, "%s with id %s is not active"),
    USER_BANNED(1004, "User %s is banned"),
    USER_DELETED(1005, "User %s is deleted"),
    RESOURCE_EXISTS(1006, "%s already exists"),
    FORBIDDEN(1007, "Forbidden access"),
    CAN_NOT_DELETE(1008, "Can not delete %s"),

    // Authentication errors (1100-1199)
    UNAUTHORIZED(1100, "Unauthorized access"),
    INVALID_CREDENTIALS(1101, "Invalid username or password"),
    TOKEN_EXPIRED(1102, "Authentication token expired"),

    // Payment errors (1200-1299)
    INSUFFICIENT_BALANCE(1200, "Insufficient balance. Required: %s, Available: %s"),
    PAYMENT_PROCESSING_ERROR(1201, "Payment processing failed: %s"),
    PAYMENT_LINK_GET_FAILED(1202, "Error when get payment link %s"),
    PAYMENT_LINK_EXPIRED(1203, "Payment link %s has expired"),
    PAYMENT_LINK_CREATE_FAILED(1204, "Failed to create payment link %s"),

    // Application errors (1300-1399)
    EXISTING_USERNAME_OR_EMAIL(1301, "Username or email already exists."),
    EXISTING_USERNAME(1302, "Username %s already exists."),
    EXISTING_EMAIL(1303, "Email %s already exists."),
    EXISTING_PHONE(1304, "Phone %s already exists."),
    PASSWORD_DOES_NOT_MATCH(1305, "Password does not match."),
    OTP_EXPIRED(1306, "OTP has expired."),
    INVALID_OTP(1307, "Invalid OTP."),
    USED_OTP(1308, "OTP has already been used."),
    RESET_TOKEN_EXPIRED(1309, "Password reset token has expired."),
    MINIMUM_AMOUNT(1310, "Minimum amount is %s, your amount is %s."),
    WITHDRAW_INTERVAL(1311, "You can only withdraw once every %s days."),
    TRANSACTION_STATUS(1312, "Transaction status is %s."),
    ACCOUNT_INFO(1313, "Account information is incomplete."),
    // API errors
    INVALID_JWT(2300, "Invalid JWT token"),
    EXPIRED_JWT(2301, "Expired JWT token"),
    INVALID_REFRESH_TOKEN(2302, "Invalid refresh token"),
    UNSUPPORTED_JWT(2303, "Unsupported JWT token"),
    INVALID_JWT_CLAIMS(2304, "JWT claim error"),
    INVALID_HASH(2305, "Invalid hash"),
    // System errors (5000-5999)
    INTERNAL_ERROR(5000, "Internal server error"),
    SERVICE_UNAVAILABLE(5001, "Service temporarily unavailable"),
    DATABASE_ERROR(5002, "Database operation failed"),
    FLEXIBLE_ERROR(9999, "%s");
    @Getter
    private final int code;
    private final String messageTemplate;
    public String getMessage(Object... args) {
        return String.format(messageTemplate, args);
    }
}
