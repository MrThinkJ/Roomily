package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.CreatePaymentLinkRequest;
import com.c2se.roomily.payload.response.CheckoutResponse;
import com.c2se.roomily.payload.response.PaymentLinkResponse;
import com.c2se.roomily.service.PaymentProcessingService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {
    PaymentProcessingService paymentProcessingService;

    @PostMapping("/create")
    public ResponseEntity<CheckoutResponse> createPaymentLink(@RequestBody CreatePaymentLinkRequest
                                                                      createPaymentLinkRequest) {
        return ResponseEntity.ok(paymentProcessingService.createPaymentLink(createPaymentLinkRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentLinkResponse> getPaymentLinkDataById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentProcessingService.getPaymentLinkData(id));
    }

    @GetMapping("/checkout/{checkoutId}")
    public ResponseEntity<CheckoutResponse> getPaymentLinkCheckoutData(@PathVariable String checkoutId) {
        return ResponseEntity.ok(paymentProcessingService.getPaymentLinkCheckoutData(checkoutId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentLinkResponse> cancelPaymentLink(@PathVariable Long id) {
        return ResponseEntity.ok(paymentProcessingService.cancelPaymentLink(id));
    }

    @PostMapping("/confirm-webhook")
    public ResponseEntity<ObjectNode> confirmWebhook(@RequestBody Map<String, String> requestBody) {
        return ResponseEntity.ok(paymentProcessingService.confirmWebhook(requestBody.get("webhookUrl")));
    }

    @PostMapping("/transfer-handler")
    public ResponseEntity<ObjectNode> transferHandler(@RequestBody ObjectNode body) {
        paymentProcessingService.payosTransferHandler(body);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test/{rentedRoomId}/{amount}")
    public ResponseEntity<ObjectNode> test(@PathVariable String rentedRoomId, @PathVariable Double amount) {
        paymentProcessingService.mockTopUpToRoomWallet(rentedRoomId, amount);
        return ResponseEntity.ok().build();
    }
}
