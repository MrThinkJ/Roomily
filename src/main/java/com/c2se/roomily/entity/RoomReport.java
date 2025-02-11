package com.c2se.roomily.entity;

import com.c2se.roomily.enums.ReportStatus;
import com.c2se.roomily.enums.RoomReportType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_reports")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RoomReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "room_report_id")
    private String id;
    private String content;
    private ReportStatus status;
    @Column(name = "report_type")
    private RoomReportType type;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;
}
