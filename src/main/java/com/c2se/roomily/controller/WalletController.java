package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.WithdrawInfoRequest;
import com.c2se.roomily.payload.response.WithdrawInfoResponse;
import com.c2se.roomily.service.WalletService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController extends BaseController{
    private final WalletService walletService;

    @GetMapping("/withdraw-info")
    public ResponseEntity<WithdrawInfoResponse> getWithdrawInfo() {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(walletService.getWithdrawInfo(userId));
    }

    @PostMapping("/withdraw-info")
    public ResponseEntity<Void> updateWithdrawInfo(@RequestBody WithdrawInfoRequest request) {
        String userId = this.getUserInfo().getId();
        walletService.updateWithdrawInfo(userId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/withdraw/{amount}")
    public ResponseEntity<Void> withdraw(@PathVariable String amount) {
        String userId = this.getUserInfo().getId();
        walletService.withdraw(userId, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm-withdraw/{transactionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> confirmWithdraw(@PathVariable String transactionId) {
        walletService.confirmWithdraw(transactionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel-withdraw/{transactionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelWithdraw(@PathVariable String transactionId,
                                               @RequestBody ObjectNode reason) {
        String reasonString = reason.get("reason").asText();
        walletService.cancelWithdraw(transactionId, reasonString);
        return ResponseEntity.ok().build();
    }
}
