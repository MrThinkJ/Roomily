package com.c2se.roomily.service;

import com.c2se.roomily.payload.internal.CreateRentDepositPaymentLinkRequest;
import com.c2se.roomily.payload.request.CreatePaymentLinkRequest;
import com.c2se.roomily.payload.response.CheckoutResponse;
import com.c2se.roomily.payload.response.PaymentLinkResponse;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface PaymentProcessingService {
    void createPaymentLink(CreateRentDepositPaymentLinkRequest paymentLinkRequest, String userId);
    CheckoutResponse createPaymentLink(CreatePaymentLinkRequest paymentLinkRequest, String userId);
    PaymentLinkResponse getPaymentLinkData(long paymentLinkId);
    CheckoutResponse getPaymentLinkCheckoutData(String checkoutId);
    PaymentLinkResponse cancelPaymentLink(long paymentLinkId);
    ObjectNode confirmWebhook(String webhookUrl);
    void payosTransferHandler(ObjectNode body);
    void mockTopUpToRoomWallet(String rentedRoomId, double amount);
}
