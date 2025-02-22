package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.CreateUserReportRequest;
import com.c2se.roomily.payload.response.UserReportResponse;
import com.c2se.roomily.payload.response.UserReportSummary;
import com.c2se.roomily.service.UserReportService;
import com.c2se.roomily.util.AppConstants;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/user-reports")
public class UserReportController extends BaseController {
    UserReportService userReportService;

    @GetMapping("/reported/{reportedUserId}")
    public ResponseEntity<List<UserReportResponse>> getUserReportsByReportedUserId(
            @PathVariable String reportedUserId,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size) {
        return ResponseEntity.ok(userReportService.getUserReportsByReportedUserId(reportedUserId, page, size));
    }

    @GetMapping("/reporters/{reporterId}")
    public ResponseEntity<List<UserReportResponse>> getUserReportsByReporterId(
            @PathVariable String reporterId,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size) {
        return ResponseEntity.ok(userReportService.getUserReportsByReporterId(reporterId, page, size));
    }

    @GetMapping("/top")
    public ResponseEntity<List<UserReportSummary>> getTopReportedUsers(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(userReportService.getTopReportedUsers(limit));
    }

    @PatchMapping("/{reportId}/read")
    public ResponseEntity<Boolean> markUserReportAsRead(@PathVariable String reportId) {
        return ResponseEntity.ok(userReportService.markUserReportAsRead(reportId));
    }

    @PatchMapping("/users/{userId}/read-all")
    public ResponseEntity<Boolean> markAllUserReportsAsRead(@PathVariable String userId) {
        return ResponseEntity.ok(userReportService.markAllUserReportsAsRead(userId));
    }

    @PostMapping("/{reportedUserId}")
    public ResponseEntity<Boolean> reportUser(
            @PathVariable String reportedUserId,
            @RequestBody CreateUserReportRequest createUserReportRequest) {
        String reporterId = this.getUserInfo().getId();
        createUserReportRequest.setReporterId(reporterId);
        return ResponseEntity.ok(userReportService.reportUser(reporterId, reportedUserId, createUserReportRequest));
    }

    @GetMapping("/check/{reportedUserId}")
    public ResponseEntity<Boolean> hasAlreadyReported(@PathVariable String reportedUserId) {
        String reporterId = this.getUserInfo().getId();
        return ResponseEntity.ok(userReportService.hasAlreadyReported(reporterId, reportedUserId));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<UserReportResponse>> getRecentReports(
            @RequestParam(value = "days", defaultValue = "7") Integer days,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size) {
        return ResponseEntity.ok(userReportService.getRecentReports(days, page, size));
    }

    @GetMapping("/threshold")
    public ResponseEntity<List<UserReportSummary>> getUsersWithReportCountAboveThreshold(
            @RequestParam(value = "threshold", defaultValue = "5") Integer threshold,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size) {
        return ResponseEntity.ok(userReportService.getUsersWithReportCountAboveThreshold(threshold, page, size));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<UserReportResponse>> getPendingReports(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) Integer page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) Integer size) {
        return ResponseEntity.ok(userReportService.getPendingReports(page, size));
    }

    @PatchMapping("/{reportId}/process")
    public ResponseEntity<Boolean> processReport(
            @PathVariable String reportId,
            @RequestParam Boolean isValid) {
        return ResponseEntity.ok(userReportService.processReport(reportId, isValid));
    }
}
