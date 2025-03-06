package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.Transaction;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.enums.TransactionStatus;
import com.c2se.roomily.enums.TransactionType;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.internal.PayOsTransactionDto;
import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.payload.request.CreatePaymentLinkRequest;
import com.c2se.roomily.payload.response.CheckoutResponse;
import com.c2se.roomily.payload.response.PaymentLinkResponse;
import com.c2se.roomily.repository.TransactionRepository;
import com.c2se.roomily.service.NotificationService;
import com.c2se.roomily.service.PaymentProcessingService;
import com.c2se.roomily.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentProcessingServiceImpl implements PaymentProcessingService {
    PayOS payOS;
    TransactionRepository transactionRepository;
    UserService userService;
    NotificationService notificationService;

    @Override
    public CheckoutResponse createPaymentLink(CreatePaymentLinkRequest paymentLinkRequest) {
        log.info("Creating payment link for amount: {}", paymentLinkRequest.getPrice());
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//        User user = userRepository.findByUsernameOrEmail(userDetails.getUsername(), userDetails.getUsername())
//                .orElseThrow(() -> new ResourceNotFoundException("User", "username", userDetails.getUsername()));
        try {
            final String productName = paymentLinkRequest.getProductName();
            final String description = paymentLinkRequest.getDescription();
            final String returnUrl = "/success";
            final String cancelUrl = "/cancel";
            final int price = paymentLinkRequest.getPrice();

            String currentTimeString = String.valueOf(new Date().getTime());
            long orderCode = Long.parseLong(currentTimeString.substring(currentTimeString.length() - 6));

            ItemData item = ItemData.builder().name(productName).price(price).quantity(1).build();

            PaymentData paymentData = PaymentData.builder().orderCode(orderCode).description(description).amount(price)
                    .item(item).returnUrl(returnUrl).cancelUrl(cancelUrl).build();

            CheckoutResponseData data = payOS.createPaymentLink(paymentData);
            Transaction transaction = Transaction.builder()
                    .amount(BigDecimal.valueOf(data.getAmount()))
                    .status(TransactionStatus.PENDING)
                    .type(TransactionType.DEPOSIT)
//                    .user(user)
                    .paymentId(data.getPaymentLinkId())
                    .updatedAt(LocalDateTime.now())
                    .build();
            transactionRepository.save(transaction);
//            log.info("Successfully created payment link for user: {}", userDetails.getUsername());
            return CheckoutResponse.builder()
                    .accountNumber(data.getAccountNumber())
                    .accountName(data.getAccountName())
                    .amount(data.getAmount())
                    .description(data.getDescription())
                    .checkoutUrl(data.getCheckoutUrl())
                    .qrCode(data.getQrCode())
                    .orderCode(data.getOrderCode())
                    .status(data.getStatus())
                    .build();
        } catch (Exception e) {
            log.error("Payment link creation failed: {}", e.getMessage(), e);
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.PAYMENT_PROCESSING_ERROR,
                    " .Error: " + e.getMessage());
        }
    }

    @Override
    public PaymentLinkResponse getPaymentLinkData(long paymentLinkId) {
        try {
            PaymentLinkData data = payOS.getPaymentLinkInformation(paymentLinkId);
            return mapToPaymentLinkDto(data);
        } catch (Exception e) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.PAYMENT_LINK_GET_FAILED,
                    paymentLinkId + ". Error: " + e.getMessage());
        }
    }

    @Override
    public PaymentLinkResponse cancelPaymentLink(long paymentLinkId) {
        try {
            PaymentLinkData data = payOS.cancelPaymentLink(paymentLinkId, null);
            Transaction transaction = transactionRepository.findByPaymentId(data.getId());
            transaction.setStatus(TransactionStatus.CANCELLED);
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            CreateNotificationRequest notification = CreateNotificationRequest.builder()
                    .header("Hủy giao dịch")
                    .body("Thanh toán của bạn đã bị hủy.")
                    .userId(transaction.getUser().getId())
                    .build();
            notificationService.sendNotification(notification);
            log.info("Successfully cancelled payment link for user: {}", transaction.getUser().getUsername());
            return mapToPaymentLinkDto(data);
        } catch (Exception e) {
            log.error("Failed to cancel payment link: {}", e.getMessage(), e);
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.PAYMENT_PROCESSING_ERROR,
                    "Failed when cancel payment. Error: " + e.getMessage());
        }
    }

    @Override
    public ObjectNode confirmWebhook(String webhookUrl) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode response = objectMapper.createObjectNode();
        try {
            String str = payOS.confirmWebhook(webhookUrl);
            response.set("data", objectMapper.valueToTree(str));
            response.put("error", 0);
            return response;
        } catch (Exception e) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.PAYMENT_PROCESSING_ERROR,
                    "Failed to confirm webhook. Error: " + e.getMessage());
        }
    }

    @Override
    public void payosTransferHandler(ObjectNode body) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Webhook webhookBody = Webhook.builder()
                    .code(body.findValue("code").asText())
                    .desc(body.findValue("desc").asText())
                    .success(body.findValue("desc").asText().equals("success"))
                    .data(objectMapper.treeToValue(body.findValue("data"), WebhookData.class))
                    .signature(body.findValue("signature").asText())
                    .build();
            WebhookData data = payOS.verifyPaymentWebhookData(webhookBody);
            if (webhookBody.getSuccess()) {
                if (data.getOrderCode() == 123) {
                    return;
                }
                PaymentLinkData paymentLinkData = payOS.getPaymentLinkInformation(data.getOrderCode());
                if (paymentLinkData == null) {
                    log.error("Payment link not found for order code: {}", data.getOrderCode());
                    throw new ResourceNotFoundException("PaymentLink", "orderCode", data.getOrderCode().toString());
                }
                Transaction transaction = transactionRepository.findByPaymentId(data.getPaymentLinkId());
                if (transaction == null) {
                    log.error("Transaction not found for payment link: {}", data.getPaymentLinkId());
                    throw new ResourceNotFoundException("Transaction", "paymentId", data.getPaymentLinkId());
                }
                if (data.getAmount() < transaction.getAmount().doubleValue()) {
                    CreateNotificationRequest notification = CreateNotificationRequest.builder()
                            .header("Thanh toán chưa đủ tiền")
                            .body("Bạn chưa thanh toán đủ số tiền, vui lòng thanh toán thêm.")
                            .userId(transaction.getUser().getId())
                            .build();
                    notificationService.sendNotification(notification);
                    log.error("Payment link amount remaining is greater than 0 for order code: {}", data.getOrderCode());
                    return;
                }
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setUpdatedAt(LocalDateTime.now());
                transactionRepository.save(transaction);

                User user = transaction.getUser();
                user.setBalance(user.getBalance().add(BigDecimal.valueOf(data.getAmount())));
                userService.saveUser(user);

                CreateNotificationRequest notification = CreateNotificationRequest.builder()
                        .header("Thanh toán thành công")
                        .body("Thanh toán của bạn đã thực hiện thành công.")
                        .userId(user.getId())
                        .build();
                notificationService.sendNotification(notification);
                log.info("Successfully handled payos transfer for user: {}", user.getUsername());
            } else {
                Transaction transaction = transactionRepository.findByPaymentId(data.getPaymentLinkId());
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                CreateNotificationRequest notification = CreateNotificationRequest.builder()
                        .header("Thanh toán thất bại")
                        .body("Thanh toán của bạn đã thất bại. Vui lòng thử lại.")
                        .userId(transaction.getUser().getId())
                        .build();
                notificationService.sendNotification(notification);
                log.error("Failed to handle payos transfer for payment link: {}", data.getPaymentLinkId());
            }
        } catch (Exception e) {
            log.error("Failed to handle payos transfer: {}", e.getMessage(), e);
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.PAYMENT_PROCESSING_ERROR,
                    "Failed to handle payos transfer. Error: " + e.getMessage());
        }
    }

    private PayOsTransactionDto mapTransactionData(vn.payos.type.Transaction transaction) {
        return PayOsTransactionDto.builder()
                .reference(transaction.getReference())
                .amount(transaction.getAmount())
                .accountNumber(transaction.getAccountNumber())
                .description(transaction.getDescription())
                .transactionDateTime(transaction.getTransactionDateTime())
                .build();
    }

    private PaymentLinkResponse mapToPaymentLinkDto(PaymentLinkData data) {
        return PaymentLinkResponse.builder()
                .id(data.getId())
                .amount(data.getAmount())
                .amountPaid(data.getAmountPaid())
                .amountRemaining(data.getAmountRemaining())
                .orderCode(data.getOrderCode())
                .status(data.getStatus())
                .createdAt(data.getCreatedAt())
                .cancellationReason(data.getCancellationReason())
                .canceledAt(data.getCanceledAt())
                .transactions(data.getTransactions().stream()
                        .map(this::mapTransactionData).collect(Collectors.toList()))
                .build();
    }
}
