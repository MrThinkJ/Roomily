package com.c2se.roomily.repository;

import com.c2se.roomily.entity.RoomBoost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomBoostRepository extends JpaRepository<RoomBoost, String> {

    List<RoomBoost> findByUserId(String userId);

    List<RoomBoost> findByRoomId(String roomId);

    @Query("SELECT rb FROM RoomBoost rb WHERE rb.active = true AND rb.startDate <= :now AND (rb.endDate IS NULL OR rb.endDate >= :now)")
    List<RoomBoost> findActiveBoosts(LocalDateTime now);

    @Query("SELECT rb FROM RoomBoost rb WHERE rb.active = true AND rb.startDate <= :now AND (rb.endDate IS NULL OR rb.endDate >= :now) AND rb.room.id = :roomId")
    List<RoomBoost> findActiveBoostsByRoomId(String roomId, LocalDateTime now);

//    @Query("SELECT rb FROM RoomBoost rb WHERE rb.active = true AND rb.startDate <= :now AND (rb.endDate IS NULL OR rb.endDate >= :now) AND rb.user.id = :userId")
//    List<RoomBoost> findActiveBoostsByUserId(String userId, LocalDateTime now);

//    @Query("SELECT rb FROM RoomBoost rb WHERE rb.active = true AND rb.startDate <= :now AND (rb.endDate IS NULL OR rb.endDate >= :now) " +
//            "AND (:city IS NULL OR rb.targetCity = :city) " +
//            "AND (:district IS NULL OR rb.targetDistrict = :district) " +
//            "AND (:ward IS NULL OR rb.targetWard = :ward)")
//    List<RoomBoost> findActiveBoostsByLocation(String city, String district, String ward, LocalDateTime now);

//    @Query("SELECT rb FROM RoomBoost rb WHERE rb.active = true AND rb.startDate <= :now AND (rb.endDate IS NULL OR rb.endDate >= :now) " +
//            "ORDER BY rb.boostLevel DESC, rb.creditsUsed DESC")
//    List<RoomBoost> findActiveBoostedRooms(LocalDateTime now);

    @Query("SELECT rb.room.id FROM RoomBoost rb WHERE rb.active = true AND rb.startDate <= :now AND (rb.endDate IS NULL OR rb.endDate >= :now) " +
            "ORDER BY rb.boostLevel DESC, rb.creditsUsed DESC")
    List<String> findActiveBoostedRoomIds(LocalDateTime now);

//    @Query("SELECT rb.room.id FROM RoomBoost rb WHERE rb.active = true AND rb.startDate <= :now AND (rb.endDate IS NULL OR rb.endDate >= :now) " +
//            "AND (:city IS NULL OR rb.targetCity = :city) " +
//            "AND (:district IS NULL OR rb.targetDistrict = :district) " +
//            "AND (:ward IS NULL OR rb.targetWard = :ward) " +
//            "ORDER BY rb.boostLevel DESC, rb.creditsUsed DESC")
//    List<String> findActiveBoostedRoomIdsByLocation(String city, String district, String ward, LocalDateTime now);

    @Query("SELECT COUNT(rb) > 0 FROM RoomBoost rb WHERE rb.room.id = :roomId AND rb.active = true " +
            "AND rb.startDate <= :now AND (rb.endDate IS NULL OR rb.endDate >= :now)")
    boolean isRoomBoosted(String roomId, LocalDateTime now);
}
