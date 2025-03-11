package com.c2se.roomily.repository;

import com.c2se.roomily.entity.RentedRoomActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentedRoomActivityRepository extends JpaRepository<RentedRoomActivity, String> {
    @Query(value = """
            SELECT * FROM rented_room_activity
            WHERE rented_room_id = :rentedRoomId
            AND (created_at, rented_room_activity_id) < (:timestamp, :pivotId)
            ORDER BY created_at DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<RentedRoomActivity> findByRentedRoomId(@Param("rentedRoomId") String rentedRoomId,
                                                @Param("pivotId") String pivotId,
                                                @Param("timestamp") String timestamp,
                                                @Param("limit") int limit);
}
