package com.c2se.roomily.controller;

import com.c2se.roomily.enums.TransactionStatus;
import com.c2se.roomily.enums.TransactionType;
import com.c2se.roomily.enums.UserStatus;
import com.c2se.roomily.payload.response.AdminDashboardResponse;
import com.c2se.roomily.payload.response.PageUserResponse;
import com.c2se.roomily.payload.response.SystemStatisticsResponse;
import com.c2se.roomily.payload.response.TransactionPageResponse;
import com.c2se.roomily.service.StatisticsService;
import com.c2se.roomily.service.TransactionService;
import com.c2se.roomily.service.UserService;
import com.c2se.roomily.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController extends BaseController {
    private final UserService userService;
    private final TransactionService transactionService;
    private final StatisticsService statisticsService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboardStats() {
        return ResponseEntity.ok(getAdminDashboardStats());
    }
    
    @GetMapping("/system-statistics")
    public ResponseEntity<SystemStatisticsResponse> getSystemStatistics() {
        return ResponseEntity.ok(getSystemStats());
    }
    
    @GetMapping("/users")
    public ResponseEntity<PageUserResponse> getAllUsers(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir
    ) {
        return ResponseEntity.ok(userService.getUsers(page, size, sortBy, sortDir));
    }
    
    @GetMapping("/users/status/{status}")
    public ResponseEntity<PageUserResponse> getUsersByStatus(
            @PathVariable String status,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir
    ) {
        return ResponseEntity.ok(userService.getUsersByStatus(status, page, size, sortBy, sortDir));
    }
    
    @GetMapping("/users/verified/{verified}")
    public ResponseEntity<PageUserResponse> getUsersByVerificationStatus(
            @PathVariable Boolean verified,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir
    ) {
        return ResponseEntity.ok(userService.getUsersByIsVerified(verified, page, size, sortBy, sortDir));
    }
    
    @GetMapping("/transactions")
    public ResponseEntity<TransactionPageResponse> getAllTransactions(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir
    ) {
        return ResponseEntity.ok(transactionService.getAllTransactions(page, size, sortBy, sortDir));
    }
    
    @GetMapping("/transactions/status/{status}")
    public ResponseEntity<TransactionPageResponse> getTransactionsByStatus(
            @PathVariable String status,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir
    ) {
        return ResponseEntity.ok(transactionService.getTransactionsByStatus(status, page, size, sortBy, sortDir));
    }
    
    @GetMapping("/transactions/type/{type}")
    public ResponseEntity<TransactionPageResponse> getTransactionsByTypeAndStatus(
            @PathVariable String type,
            @RequestParam(value = "status", defaultValue = "COMPLETED") String status,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir
    ) {
        return ResponseEntity.ok(
                transactionService.getTransactionsByTypeAndStatus(type, status, page, size, sortBy, sortDir));
    }
    
    @GetMapping("/transactions/pending-withdrawals")
    public ResponseEntity<TransactionPageResponse> getPendingWithdrawals(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir
    ) {
        return ResponseEntity.ok(
                transactionService.getTransactionsByTypeAndStatus(
                        TransactionType.WITHDRAWAL.name(), 
                        TransactionStatus.PENDING.name(), 
                        page, size, sortBy, sortDir));
    }
    
    private AdminDashboardResponse getAdminDashboardStats() {
        long totalUsers = userService.getTotalUsers();
        long totalLandlords = userService.getTotalLandlords();
        long totalTenants = userService.getTotalTenants();
        long activeUsers = userService.getUserCountByStatus(UserStatus.ACTIVE);
        long newUsersThisMonth = userService.getNewUserCountThisMonth();
        BigDecimal totalSystemBalance = userService.getTotalSystemBalance();
        BigDecimal totalWithdrawalsThisMonth = transactionService.getTotalCompletedWithdrawalsThisMonth();
        BigDecimal totalDepositsThisMonth = transactionService.getTotalCompletedDepositsThisMonth();
        long pendingWithdrawalsCount = transactionService.getPendingWithdrawalsCount();
        
        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalLandlords(totalLandlords)
                .totalTenants(totalTenants)
                .activeUsers(activeUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .totalSystemBalance(totalSystemBalance)
                .totalWithdrawalsThisMonth(totalWithdrawalsThisMonth)
                .totalDepositsThisMonth(totalDepositsThisMonth)
                .pendingWithdrawalsCount(pendingWithdrawalsCount)
                .build();
    }
    
    private SystemStatisticsResponse getSystemStats() {
        return SystemStatisticsResponse.builder()
                .totalRentedRooms(statisticsService.getTotalRentedRooms())
                .totalActiveRentedRooms(statisticsService.getTotalActiveRentedRooms())
                .totalUsers(userService.getTotalUsers())
                .totalActiveUsers(userService.getUserCountByStatus(UserStatus.ACTIVE))
                .totalCompletedTransactions(transactionService.getCompletedTransactionsCount())
                .totalTransactionVolume(transactionService.getTotalTransactionVolume())
                .build();
    }

}
