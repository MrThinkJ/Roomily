package com.c2se.roomily.repository;

import com.c2se.roomily.entity.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomImageRepository extends JpaRepository<RoomImage, String> {
    @Query("SELECT url FROM RoomImage WHERE room.id = :roomId")
    List<String> findUrlsByRoomId(@Param("roomId") String roomId);
    List<RoomImage> findByRoomId(String roomId);
    List<RoomImage> findByRoomIdAndIdIn(String roomId, List<String> imageIds);
}
