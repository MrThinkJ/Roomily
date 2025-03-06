package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.CreateUserReportRequest;
import com.c2se.roomily.payload.response.UserReportResponse;
import com.c2se.roomily.payload.response.UserReportSummary;

import java.util.List;

public interface UserReportService {
    List<UserReportResponse> getUserReportsByReportedUserId(String reportedUserId, Integer page, Integer size);

    List<UserReportResponse> getUserReportsByReporterId(String reporterId, Integer page, Integer size);

    List<UserReportSummary> getTopReportedUsers(Integer limit);

    Boolean markUserReportAsRead(String userReportId);

    Boolean markAllUserReportsAsRead(String userId);

    Boolean reportUser(String reporterId, String reportedUserId, CreateUserReportRequest createUserReportRequest);

    Boolean hasAlreadyReported(String reporterId, String reportedUserId);

    List<UserReportResponse> getRecentReports(Integer days, Integer page, Integer size);

    List<UserReportSummary> getUsersWithReportCountAboveThreshold(Integer threshold, Integer page, Integer size);

    List<UserReportResponse> getPendingReports(Integer page, Integer size);

    Boolean processReport(String reportId, Boolean isValid);
}
