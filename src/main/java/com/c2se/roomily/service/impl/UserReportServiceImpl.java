package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.User;
import com.c2se.roomily.entity.UserReport;
import com.c2se.roomily.enums.ReportStatus;
import com.c2se.roomily.enums.UserReportType;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.BanUserRequest;
import com.c2se.roomily.payload.request.CreateUserReportRequest;
import com.c2se.roomily.payload.response.PageResponse;
import com.c2se.roomily.payload.response.UserReportPageResponse;
import com.c2se.roomily.payload.response.UserReportResponse;
import com.c2se.roomily.payload.response.UserReportSummary;
import com.c2se.roomily.repository.UserReportRepository;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.service.BanService;
import com.c2se.roomily.service.UserReportService;
import com.c2se.roomily.util.AppConstants;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserReportServiceImpl implements UserReportService {
    UserRepository userRepository;
    UserReportRepository userReportRepository;
    BanService banService;

    @Override
    public List<UserReportResponse> getUserReportsByReportedUserId(String reportedUserId, Integer page, Integer size) {
        User user = userRepository.findById(reportedUserId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", reportedUserId)
        );
        Pageable pageable = PageRequest.of(page, size);
        Page<UserReport> userReports = userReportRepository.findByReportedUserId(user.getId(), pageable);
        return getUserReportPageResponse(userReports).getUserReportResponses();
    }

    @Override
    public List<UserReportResponse> getUserReportsByReporterId(String reporterId, Integer page, Integer size) {
        User user = userRepository.findById(reporterId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", reporterId)
        );
        Pageable pageable = PageRequest.of(page, size);
        Page<UserReport> userReports = userReportRepository.findByReportedUserId(user.getId(), pageable);
        return getUserReportPageResponse(userReports).getUserReportResponses();
    }

    @Override
    public List<UserReportSummary> getTopReportedUsers(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return userReportRepository.findTopReportedUsers(pageable);
    }

    @Override
    public Boolean markUserReportAsRead(String userReportId) {
        UserReport report = userReportRepository.findById(userReportId)
                .orElseThrow(() -> new ResourceNotFoundException("UserReport", "id", userReportId));
        report.setStatus(ReportStatus.PROCESSED);
        userReportRepository.save(report);
        return true;
    }

    @Override
    public Boolean markAllUserReportsAsRead(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        userReportRepository.markUserReportAsProcessed(ReportStatus.PROCESSED, user.getId());
        return true;
    }

    @Override
    public Boolean reportUser(String reporterId, String reportedUserId,
                              CreateUserReportRequest createUserReportRequest) {
        if (hasAlreadyReported(reporterId, reportedUserId))
            return false;
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reporterId));

        User reportedUser = userRepository.findById(reportedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reportedUserId));

        UserReport report = UserReport.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .content(createUserReportRequest.getContent())
                .type(UserReportType.valueOf(createUserReportRequest.getType()))
                .status(ReportStatus.PENDING)
                .build();
        userReportRepository.save(report);
        return true;
    }

    @Override
    public Boolean hasAlreadyReported(String reporterId, String reportedUserId) {
        return userReportRepository.existsByReporterIdAndReportedUserIdAndStatus(
                reporterId, reportedUserId, ReportStatus.PENDING);
    }

    @Override
    public List<UserReportResponse> getRecentReports(Integer days, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserReport> userReports = userReportRepository.findByCreatedAtAfter(
                java.time.LocalDateTime.now().minusDays(days), pageable);
        return getUserReportPageResponse(userReports).getUserReportResponses();
    }

    @Override
    public List<UserReportSummary> getUsersWithReportCountAboveThreshold(Integer threshold, Integer page,
                                                                         Integer size) {
        Pageable pageable = PageRequest.of(page, size);
//        return userReportRepository.findUsersWithReportCountAboveThreshold(threshold, pageable);
        return null;
    }

    @Override
    public List<UserReportResponse> getPendingReports(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserReport> pendingReports = userReportRepository.findByStatus(ReportStatus.PENDING, pageable);
        return pendingReports.getContent().stream()
                .map(this::mapToUserReportResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean processReport(String reportId, Boolean isValid) {
        UserReport report = userReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("UserReport", "id", reportId));
        report.setStatus(ReportStatus.PROCESSED);
        report.setIsValid(isValid);
        userReportRepository.save(report);
        if (isValid) {
            long validReportCount = userReportRepository.countValidReportsByReportedUserId(
                    report.getReportedUser().getId());
            if (validReportCount >= AppConstants.VALID_REPORT_THRESHOLD) { // Configurable threshold
                BanUserRequest banUserRequest = BanUserRequest.builder()
                        .userId(report.getReportedUser().getId())
                        .reason("Multiple valid reports received")
                        .expiresAt(LocalDateTime.now().plusDays(AppConstants.DEFAULT_BAN_DURATION))
                        .build();
                banService.banUser(banUserRequest);
            }
        }
        return true;
    }

    private UserReportResponse mapToUserReportResponse(UserReport userReport) {
        return UserReportResponse.builder()
                .id(userReport.getId())
                .reportedUserId(userReport.getReportedUser().getId())
                .reporterId(userReport.getReporter().getId())
                .content(userReport.getContent())
                .createdAt(userReport.getCreatedAt())
                .status(userReport.getStatus().name())
                .type(userReport.getType().name())
                .build();
    }

    private UserReportPageResponse getUserReportPageResponse(Page<UserReport> userReports) {
        return UserReportPageResponse.builder()
                .userReportResponses(userReports.getContent().stream()
                                             .map(this::mapToUserReportResponse)
                                             .collect(Collectors.toList()))
                .pageResponse(PageResponse.builder()
                                      .currentPage(userReports.getNumber())
                                      .totalPages(userReports.getTotalPages())
                                      .totalElements(userReports.getTotalElements())
                                      .hasNext(userReports.hasNext())
                                      .hasPrevious(userReports.hasPrevious())
                                      .build())
                .build();
    }
}
