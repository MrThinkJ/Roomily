package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.*;
import com.c2se.roomily.entity.Transaction;
import com.c2se.roomily.enums.*;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.internal.CreateRentDepositPaymentLinkRequest;
import com.c2se.roomily.payload.internal.PayOsTransactionDto;
import com.c2se.roomily.payload.request.CreateNotificationRequest;
import com.c2se.roomily.payload.request.CreatePaymentLinkRequest;
import com.c2se.roomily.payload.request.CreateRentedRoomActivityRequest;
import com.c2se.roomily.payload.response.CheckoutResponse;
import com.c2se.roomily.payload.response.PaymentLinkResponse;
import com.c2se.roomily.repository.CheckoutInfoRepository;
import com.c2se.roomily.repository.TransactionRepository;
import com.c2se.roomily.security.CustomUserDetails;
import com.c2se.roomily.service.*;
import com.c2se.roomily.util.UtilFunction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.type.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingServiceImpl implements PaymentProcessingService {
    private final PayOS payOS;
    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final CheckoutInfoRepository checkoutInfoRepository;
    private final RentedRoomService rentedRoomService;
    private final RentedRoomActivityService rentedRoomActivityService;
    private final BillLogService billLogService;
    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void createPaymentLink(CreateRentDepositPaymentLinkRequest createRentDepositPaymentLinkRequest, String userId) {
        log.info("Creating payment link for amount: {}", createRentDepositPaymentLinkRequest.getAmount());
        try {
            final String productName = createRentDepositPaymentLinkRequest.getProductName();
            final String description = createRentDepositPaymentLinkRequest.getDescription();
            final String returnUrl = "/success";
            final String cancelUrl = "/cancel";
            final int price = createRentDepositPaymentLinkRequest.getAmount();

            Random random = new Random();
            long orderCode = random.nextLong();
            ItemData item = ItemData.builder().name(productName).price(price).quantity(1).build();
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderCode)
                    .description(description)
                    .amount(price)
                    .item(item)
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .build();

            CheckoutResponseData data = payOS.createPaymentLink(paymentData);
            User user = userService.getUserEntityById(userId);
            CreatePaymentLinkRequest paymentLinkRequest = CreatePaymentLinkRequest.builder()
                    .amount(createRentDepositPaymentLinkRequest.getAmount())
                    .description(createRentDepositPaymentLinkRequest.getDescription())
                    .productName(createRentDepositPaymentLinkRequest.getProductName())
                    .rentedRoomId(createRentDepositPaymentLinkRequest.getRentedRoomId())
                    .build();
            createPaymentLinkForRentedRoomWallet(data, user, paymentLinkRequest,
                                                createRentDepositPaymentLinkRequest.getCheckoutId(),
                                                createRentDepositPaymentLinkRequest.getChatMessageId());
        } catch (Exception e) {
            log.error("Payment link creation failed: {}", e.getMessage(), e);
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.PAYMENT_PROCESSING_ERROR,
                                   " .Error: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckoutResponse createPaymentLink(CreatePaymentLinkRequest paymentLinkRequest, String userId) {
        log.info("Creating payment link for amount: {}", paymentLinkRequest.getAmount());
        try {
            String checkoutId = UtilFunction.hash(UUID.randomUUID().toString());
            final String productName = paymentLinkRequest.getProductName();
            final String description = paymentLinkRequest.getDescription();
            final boolean isInAppWallet = paymentLinkRequest.isInAppWallet();
            final String returnUrl = "/success";
            final String cancelUrl = "/cancel";
            final int price = paymentLinkRequest.getAmount();
            Random random = new Random();
            long orderCode = random.nextLong();
            ItemData item = ItemData.builder().name(productName).price(price).quantity(1).build();
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderCode)
                    .description(description)
                    .amount(price)
                    .item(item)
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .build();

            CheckoutResponseData data = payOS.createPaymentLink(paymentData);
            User user = userService.getUserEntityById(userId);
            if (isInAppWallet) {
                return createPaymentLinkForInAppWallet(data, user, checkoutId);
            } else {
                return createPaymentLinkForRentedRoomWallet(data, user, paymentLinkRequest, checkoutId, null);
            }
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
    public CheckoutResponse getPaymentLinkCheckoutData(String checkoutId) {
        return checkoutInfoRepository.findById(checkoutId)
                .orElseThrow(() -> new ResourceNotFoundException("CheckoutInfo", "id", checkoutId));
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
                    .body("Giao dịch của bạn đã bị hủy.")
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
                    log.error("Payment link amount remaining is greater than 0 for order code: {}",
                              data.getOrderCode());
                    return;
                }
                if (transaction.getType().equals(TransactionType.DEPOSIT)) {
                    handleTopUpInAppWallet(transaction, data);
                } else if (transaction.getType().equals(TransactionType.RENT_PAYMENT)) {
                    handleTopUpRentedRoomWallet(transaction, data);
                }
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

    @Override
    public void mockTopUpToRoomWallet(String rentedRoomId, double amount) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = userDetails.getId();
        User user = userService.getUserEntityById(userId);
        RentedRoom rentedRoom = rentedRoomService.getRentedRoomEntityById(rentedRoomId);
        Room room = rentedRoom.getRoom();

        // Process the payment
        BigDecimal currentBalance = rentedRoom.getRentedRoomWallet() != null ?
                rentedRoom.getRentedRoomWallet() : BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.add(BigDecimal.valueOf(amount));
        rentedRoom.setRentedRoomWallet(newBalance);

        // Check if the deposit needs to be paid
        if (rentedRoom.getStatus() == RentedRoomStatus.DEPOSIT_NOT_PAID) {
            handleDepositPaid(rentedRoom, room);
        }

        // Check if the debt needs to be paid
        else if (rentedRoom.getStatus() == RentedRoomStatus.DEBT
                || rentedRoom.getStatus() == RentedRoomStatus.BILL_MISSING) {
            handleDebtRentedRoomPaid(rentedRoom);
        } else{
            rentedRoomService.saveRentedRoom(rentedRoom);
        }
        Transaction transaction = Transaction.builder()
                .amount(BigDecimal.valueOf(amount))
                .status(TransactionStatus.COMPLETED)
                .type(TransactionType.RENT_PAYMENT)
                .user(user)
                .paymentId(null)
                .metadata(rentedRoom.getId())
                .updatedAt(LocalDateTime.now())
                .checkoutResponseId(null)
                .build();
        transactionRepository.save(transaction);
    }

    private CheckoutResponse createPaymentLinkForInAppWallet(CheckoutResponseData data,
                                                             User user,
                                                             String checkoutId) {
        CheckoutResponse checkoutResponse = buildCheckoutResponse(data);
        CheckoutResponse savedCheckoutResponse = checkoutInfoRepository.save(checkoutId,
                                                                             checkoutResponse);
        Transaction transaction = Transaction.builder()
                .amount(BigDecimal.valueOf(data.getAmount()))
                .status(TransactionStatus.PENDING)
                .type(TransactionType.DEPOSIT)
                .user(user)
                .paymentId(data.getPaymentLinkId())
                .updatedAt(LocalDateTime.now())
                .checkoutResponseId(savedCheckoutResponse.getId())
                .build();
        transactionRepository.save(transaction);
        log.info("Successfully created payment link to top up in-app wallet for user: {}", user.getUsername());
        return savedCheckoutResponse;
    }

    private CheckoutResponse createPaymentLinkForRentedRoomWallet(CheckoutResponseData data,
                                                                  User user,
                                                                  CreatePaymentLinkRequest request,
                                                                  String checkoutId,
                                                                  String chatMessageId) {
        // Build the checkout response
        CheckoutResponse checkoutResponse = buildCheckoutResponse(data);
        CheckoutResponse savedCheckoutResponse = checkoutInfoRepository.save(checkoutId, checkoutResponse);
        RentedRoom rentedRoom = rentedRoomService.getRentedRoomEntityById(request.getRentedRoomId());
        Transaction transaction = Transaction.builder()
                .amount(BigDecimal.valueOf(data.getAmount()))
                .status(TransactionStatus.PENDING)
                .type(TransactionType.RENT_PAYMENT)
                .user(user)
                .paymentId(data.getPaymentLinkId())
                .metadata(rentedRoom.getId())
                .updatedAt(LocalDateTime.now())
                .checkoutResponseId(savedCheckoutResponse.getId())
                .build();
        if (chatMessageId != null) {
            transaction.setChatMessageId(chatMessageId);
        }
        transactionRepository.save(transaction);
        log.info("Successfully created payment link to top up rented room wallet for user: {}", user.getUsername());
        return savedCheckoutResponse;
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleTopUpInAppWallet(Transaction transaction, WebhookData data) {
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        User user = transaction.getUser();
        user.setBalance(user.getBalance().add(BigDecimal.valueOf(data.getAmount())));
        userService.saveUser(user);

        CreateNotificationRequest notification = CreateNotificationRequest.builder()
                .header("Nạp tiền thành công")
                .body("Nạp tiền vào ví của bạn đã thực hiện thành công.")
                .userId(user.getId())
                .build();
        notificationService.sendNotification(notification);
        log.info("Successfully handled payos transfer for user: {}", user.getUsername());
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleTopUpRentedRoomWallet(Transaction transaction, WebhookData data) {
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        String rentedRoomId = transaction.getMetadata();
        RentedRoom rentedRoom = rentedRoomService.getRentedRoomEntityById(rentedRoomId);
        Room room = rentedRoom.getRoom();

        // Process the payment
        BigDecimal currentBalance = rentedRoom.getRentedRoomWallet() != null ?
                rentedRoom.getRentedRoomWallet() : BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.add(BigDecimal.valueOf(data.getAmount()));
        rentedRoom.setRentedRoomWallet(newBalance);

        // Check if the deposit needs to be paid
        if (rentedRoom.getStatus() == RentedRoomStatus.DEPOSIT_NOT_PAID) {
            handleDepositPaid(rentedRoom, room);
        }
        // Check if the debt needs to be paid
        else if (rentedRoom.getStatus() == RentedRoomStatus.DEBT
                || rentedRoom.getStatus() == RentedRoomStatus.BILL_MISSING) {
            handleDebtRentedRoomPaid(rentedRoom);
        } else{
            rentedRoomService.saveRentedRoom(rentedRoom);
        }

        if (transaction.getChatMessageId() != null) {
            try{
                ChatMessage chatMessage = chatMessageService.getChatMessageById(transaction.getChatMessageId());
                chatMessage.setMessage("Đã trả tiền đặt cọc thành công.");
                checkoutInfoRepository.delete(transaction.getCheckoutResponseId());
                chatMessage.setMetadata(null);
                chatMessageService.saveChatMessageEntity(chatMessage);
                List<String> users = chatRoomService.getChatRoomUserIds(chatMessage.getChatRoom().getId());
                users.forEach(user -> messagingTemplate.convertAndSendToUser(user,
                                                                             "/queue/refresh/"+
                                                                                     chatMessage.getChatRoom().getId(),
                                                                             true));
            } catch (ResourceNotFoundException e) {
                log.error("Chat message not found for ID: {}", transaction.getChatMessageId());
            }
        }
        // Send notification to tenant and rented room
        CreateRentedRoomActivityRequest activityRequest = CreateRentedRoomActivityRequest.builder()
                .rentedRoomId(rentedRoomId)
                .message(String.format("%s nạp %s cho phòng thuê", transaction.getUser().getFullName(),
                                       data.getAmount()))
                .build();
        rentedRoomActivityService.createRentedRoomActivity(activityRequest);
        CreateNotificationRequest tenantNotification = CreateNotificationRequest.builder()
                .header("Nạp vào ví phòng thành công")
                .body("Bạn đã nạp thành công " + data.getAmount() + " vào ví phòng thuê.")
                .userId(transaction.getUser().getId())
                .build();
        notificationService.sendNotification(tenantNotification);
        log.info("Successfully topped up rentedRoomWallet for room: {}, tenant: {}, amount: {}",
                 rentedRoom.getRoom().getId(), transaction.getUser().getUsername(), data.getAmount());
    }

    private void handleDepositPaid(RentedRoom rentedRoom, Room room){
        if (rentedRoom.getRentedRoomWallet().compareTo(room.getRentalDeposit()) < 0)
            return;
        roomService.updateRoomStatus(rentedRoom.getRoom().getId(), RoomStatus.RENTED.name());
        rentedRoom.setStatus(RentedRoomStatus.IN_USE);
        rentedRoom.setRentedRoomWallet(rentedRoom.getRentedRoomWallet().subtract(room.getRentalDeposit()));
        rentedRoom.setRentalDeposit(room.getRentalDeposit());
        rentedRoomService.deleteRentedRoomNotPaidDepositByRoomId(rentedRoom.getRoom().getId());
        CreateRentedRoomActivityRequest depositPaidActivity = CreateRentedRoomActivityRequest.builder()
                .rentedRoomId(rentedRoom.getId())
                .message("Đã thanh toán tiền đặt cọc.")
                .build();
        CreateNotificationRequest landlordNotification = CreateNotificationRequest.builder()
                .header("Phòng đã cọc đủ tiền")
                .body("Phòng " + rentedRoom.getRoom().getId() + " đã được cọc đủ tiền.")
                .userId(rentedRoom.getLandlord().getId())
                .build();
        rentedRoomActivityService.createRentedRoomActivity(depositPaidActivity);
        notificationService.sendNotification(landlordNotification);
    }

    private void handleDebtRentedRoomPaid(RentedRoom rentedRoom) {
        // If not in debt or bill missing state, return
        if (rentedRoom.getStatus() != RentedRoomStatus.DEBT && 
            rentedRoom.getStatus() != RentedRoomStatus.BILL_MISSING) {
            return;
        }
        // If the wallet balance is not enough to pay the debt, return
        if (rentedRoom.getRentedRoomWallet().compareTo(rentedRoom.getWalletDebt()) < 0) {
            log.error("Not enough wallet balance to pay the debt for rented room: {}", rentedRoom.getId());
            rentedRoomService.saveRentedRoom(rentedRoom);
            return;
        }
        // If the wallet balance is enough to pay the debt (already fill bill), deduct the debt from the wallet
        if (rentedRoom.getStatus() == RentedRoomStatus.DEBT) {
            // Get the active bill log for this rented room
            BillLog activeBillLog = billLogService.getActiveBillLogByRentedRoomId(rentedRoom.getId());
            if (activeBillLog == null) {
                log.error("No active bill log found for rented room: {}", rentedRoom.getId());
                return;
            }

            // Update the rentedRoom status and clear debt
            rentedRoom.setStatus(RentedRoomStatus.IN_USE);
            rentedRoom.setDebtDate(null);
            rentedRoom.setRentedRoomWallet(rentedRoom.getRentedRoomWallet().subtract(rentedRoom.getWalletDebt()));
            rentedRoom.setWalletDebt(BigDecimal.ZERO);

            // Mark the bill as paid based on whether it's late or not
            LocalDate today = LocalDate.now();
            if (activeBillLog.getBillStatus() == BillStatus.PENDING) {
                activeBillLog.setBillStatus(BillStatus.PAID);
            } else if (activeBillLog.getBillStatus() == BillStatus.UNPAID) {
                if (activeBillLog.getLateDate() != null && today.isAfter(activeBillLog.getLateDate())) {
                    activeBillLog.setBillStatus(BillStatus.LATE_PAID);
                } else {
                    activeBillLog.setBillStatus(BillStatus.PAID);
                }
            } else if (activeBillLog.getBillStatus() == BillStatus.LATE) {
                activeBillLog.setBillStatus(BillStatus.LATE_PAID);
            }
            
            billLogService.save(activeBillLog);
            log.info("Successfully paid bill for rented room: {}", rentedRoom.getId());

            // Create activity and notification
            String paymentMessage = activeBillLog.getBillStatus() == BillStatus.LATE_PAID 
                ? "Đã thanh toán muộn tiền thuê và điện nước cho tháng này."
                : "Đã thanh toán đầy đủ tiền thuê và điện nước cho tháng này.";
                
            CreateRentedRoomActivityRequest fullPaymentActivity = CreateRentedRoomActivityRequest.builder()
                    .rentedRoomId(rentedRoom.getId())
                    .message(paymentMessage)
                    .build();
            rentedRoomActivityService.createRentedRoomActivity(fullPaymentActivity);

            CreateNotificationRequest landlordNotification = CreateNotificationRequest.builder()
                    .header("Nhận thanh toán tiền thuê và điện nước")
                    .body("Phòng " + rentedRoom.getRoom().getId() + " " + 
                          (activeBillLog.getBillStatus() == BillStatus.LATE_PAID 
                              ? "đã được thanh toán muộn tiền thuê và điện nước."
                              : "đã được thanh toán đầy đủ tiền thuê và điện nước."))
                    .userId(rentedRoom.getLandlord().getId())
                    .build();
            notificationService.sendNotification(landlordNotification);
        } else {
            // For BILL_MISSING status, the bill log still needs to be filled
            // Just deduct the rental cost from the wallet
            // Get the active bill log and update its status if already created
            BillLog activeBillLog = billLogService.getActiveBillLogByRentedRoomId(rentedRoom.getId());
            if (!activeBillLog.isRentalCostPaid()){
                rentedRoom.setRentedRoomWallet(rentedRoom.getRentedRoomWallet().subtract(rentedRoom.getWalletDebt()));
                rentedRoom.setWalletDebt(BigDecimal.ZERO);
                activeBillLog.setRentalCostPaid(true);
                billLogService.save(activeBillLog);
            }
            if (activeBillLog.getBillStatus() == BillStatus.MISSING) {
                // The rental cost is already paid but still waiting for bill information
                // No status change needed as the bill is still missing
                CreateRentedRoomActivityRequest rentalPaidActivity = CreateRentedRoomActivityRequest.builder()
                        .rentedRoomId(rentedRoom.getId())
                        .message("Đã thanh toán tiền thuê phòng cho tháng này, vẫn chờ thông tin hóa đơn.")
                        .build();
                rentedRoomActivityService.createRentedRoomActivity(rentalPaidActivity);

                CreateNotificationRequest landlordNotification = CreateNotificationRequest.builder()
                        .header("Nhận thanh toán tiền thuê phòng nhưng chưa có hóa đơn")
                        .body("Phòng " + rentedRoom.getRoom().getId() + " đã được thanh toán tiền thuê phòng nhưng chưa có hóa đơn.")
                        .userId(rentedRoom.getLandlord().getId())
                        .build();
                notificationService.sendNotification(landlordNotification);
            }
        }
        log.info("Successfully handled debt payment for rented room: {}", rentedRoom.getId());
        rentedRoomService.saveRentedRoom(rentedRoom);
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

    private CheckoutResponse buildCheckoutResponse(CheckoutResponseData data) {
        return CheckoutResponse.builder()
                .accountNumber(data.getAccountNumber())
                .accountName(data.getAccountName())
                .amount(data.getAmount())
                .description(data.getDescription())
                .checkoutUrl(data.getCheckoutUrl())
                .qrCode(data.getQrCode())
                .orderCode(data.getOrderCode())
                .paymentLinkId(data.getPaymentLinkId())
                .status(data.getStatus())
                .build();
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
}
