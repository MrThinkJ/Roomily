package com.c2se.roomily.repository;

import com.c2se.roomily.entity.UserReport;
import com.c2se.roomily.enums.ReportStatus;
import com.c2se.roomily.payload.response.UserReportSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, String> {
    @Query("SELECT new com.c2se.roomily.payload.response.UserReportSummary(u.id, u.username, COUNT(r)) " +
            "FROM UserReport r JOIN r.reportedUser u " +
            "GROUP BY u.id, u.username " +
            "ORDER BY COUNT(r) DESC")
    List<UserReportSummary> findTopReportedUsers(Pageable pageable);

    Page<UserReport> findByReportedUserId(String reportedUserId, Pageable pageable);

    Page<UserReport> findByReporterId(String reporterId, Pageable pageable);

    Page<UserReport> findByCreatedAtAfter(LocalDateTime createdAt, Pageable pageable);

    Page<UserReport> findByStatus(ReportStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE UserReport r SET r.status = :status WHERE r.reportedUser.id = :reportedUserId")
    void markUserReportAsProcessed(@Param("reportStatus") ReportStatus status,
                                   @Param("reportedUserId") String reportedUserId);

    Boolean existsByReporterIdAndReportedUserIdAndStatus(String reporterId, String reportedUserId, ReportStatus status);

    @Query("SELECT COUNT(r) FROM UserReport r WHERE r.reportedUser.id = :reportedUserId AND r.isValid = true")
    Integer countValidReportsByReportedUserId(String reportedUserId);
}