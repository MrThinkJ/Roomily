package com.c2se.roomily.repository;

import com.c2se.roomily.entity.RoomReport;
import com.c2se.roomily.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomReportRepository extends JpaRepository<RoomReport, String> {
    boolean existsByReporterIdAndRoomId(String reporterId, String roomId);

    @Modifying
    @Query("update RoomReport r set r.status = :status where r.room.id = :roomId and r.status = 'pending'")
    void updateRoomReportStatusByRoomId(String roomId, @Param("status") ReportStatus status);

    Page<RoomReport> findAllByRoomId(String roomId, Pageable pageable);

    Page<RoomReport> findAllByStatus(ReportStatus status, Pageable pageable);

    Page<RoomReport> findAllByRoomIdAndStatus(String roomId, ReportStatus status, Pageable pageable);

    Page<RoomReport> findAllByReporterId(String reporterId, Pageable pageable);
}
