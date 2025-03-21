package com.c2se.roomily.repository;

import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.enums.RentedRoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RentedRoomRepository extends JpaRepository<RentedRoom, String> {
    List<RentedRoom> findByRoomId(String roomId);

    @Query("SELECT rr FROM RentedRoom rr WHERE rr.room.id = :roomId AND rr.status IN :status AND rr.endDate >= CURRENT_DATE")
    RentedRoom findActiveByRoomId(@Param("roomId") String roomId, @Param("status") List<RentedRoomStatus> status);

    @Query("SELECT rr FROM RentedRoom rr WHERE rr.user.id = :userId AND rr.status IN :status AND rr.endDate >= CURRENT_DATE")
    List<RentedRoom> findActiveByUserId(@Param("userId") String userId, @Param("status") List<RentedRoomStatus> status);

    @Query("""
             SELECT rr FROM RentedRoom rr JOIN rr.coTenants ct
             WHERE ct.id = :coTenantId
             AND rr.status IN :status
            """)
    List<RentedRoom> findActiveByCoTenantId(@Param("coTenantId") String coTenantId,
                                            @Param("status") List<RentedRoomStatus> status);

    List<RentedRoom> findByUserId(String userId);

    List<RentedRoom> findByLandlordId(String landlordId);

    List<RentedRoom> findByEndDate(LocalDate endDate);

    List<RentedRoom> findByDebtDate(LocalDate debtDate);

    @Query("""
            SELECT CASE WHEN COUNT(rr) > 0 THEN TRUE ELSE FALSE END
            FROM RentedRoom rr JOIN rr.coTenants ct
            WHERE rr.room.id = :roomId AND (rr.user.id = :userId OR ct.id = :userId)
        """)
    boolean existsByRoomIdAndUserIdOrCoTenantId(String roomId, String userId);
    @Query(
            """
            SELECT rr FROM RentedRoom rr JOIN rr.coTenants ct
            WHERE rr.room.id = :roomId AND (rr.user.id = :userId OR ct.id = :coTenantId)
            AND rr.status IN :status
            """
    )
    RentedRoom findActiveByRoomIdAndUserIdOrCoTenantId(String roomId, String userId, List<RentedRoomStatus> status);
    List<RentedRoom> findByRoomIdAndStatusIn(String roomId, List<RentedRoomStatus> status);
    @Modifying
    @Transactional
    @Query("DELETE FROM RentedRoom rr WHERE rr.room.id = :roomId AND rr.status = :status")
    void deleteByRoomIdAndStatus(String roomId, RentedRoomStatus status);
} 