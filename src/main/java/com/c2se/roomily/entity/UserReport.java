package com.c2se.roomily.entity;

import com.c2se.roomily.enums.ReportStatus;
import com.c2se.roomily.enums.UserReportType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_reports")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_report_id")
    private String id;
    @Column(name = "report_type")
    @Enumerated(EnumType.STRING)
    private UserReportType type;
    @Column(name = "report_content")
    private String content;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private ReportStatus status;
    private Boolean isValid;
    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;
    @ManyToOne
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;
}
