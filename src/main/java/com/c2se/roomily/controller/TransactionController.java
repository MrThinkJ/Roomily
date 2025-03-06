package com.c2se.roomily.controller;

import com.c2se.roomily.payload.response.TransactionPageResponse;
import com.c2se.roomily.payload.response.TransactionResponse;
import com.c2se.roomily.service.TransactionService;
import com.c2se.roomily.util.AppConstants;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    TransactionService transactionService;

    @GetMapping
    public ResponseEntity<TransactionPageResponse> getAllTransactions(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir
    ) {
        return ResponseEntity.ok(transactionService.getAllTransactions(page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable String id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteTransaction(@PathVariable String id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<TransactionPageResponse> getTransactionsByUserId(
            @PathVariable String userId,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir
    ) {
        return ResponseEntity.ok(transactionService.getTransactionsByUserId(userId, page, size, sortBy, sortDir));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<TransactionPageResponse> getTransactionsByStatus(
            @PathVariable String status,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir
    ) {
        return ResponseEntity.ok(transactionService.getTransactionsByStatus(status, page, size, sortBy, sortDir));
    }

    @GetMapping("/type/{type}/status/{status}")
    public ResponseEntity<TransactionPageResponse> getTransactionsByTypeAndStatus(
            @PathVariable String type,
            @PathVariable String status,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir
    ) {
        return ResponseEntity.ok(transactionService.getTransactionsByTypeAndStatus(type, status, page, size, sortBy, sortDir));
    }
}
