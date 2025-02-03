package com.c2se.roomily.entity;

import com.c2se.roomily.enums.UserReportStatus;
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
    private UserReportType type;
    @Column(name = "report_content")
    private String content;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    private UserReportStatus status;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
