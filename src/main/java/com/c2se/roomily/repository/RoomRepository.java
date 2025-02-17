package com.c2se.roomily.repository;

import com.c2se.roomily.entity.Room;
import com.c2se.roomily.enums.RoomStatus;
import com.c2se.roomily.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    List<Room> findByLandlordId(String landlordId);
    @Modifying
    @Query("UPDATE Room r SET r.status = :status WHERE r.id = :roomId")
    void updateRoomStatusById(String roomId, RoomStatus status);
    @Modifying
    @Query("UPDATE Room r SET r.status = :status WHERE r.landlord.id = :landlordId")
    void updateRoomStatusByLandlordId(String landlordId, RoomStatus status);
    @Query("SELECT r FROM Room r WHERE " +
            "(COALESCE(:city, '') = '' OR r.city LIKE CONCAT('%', :city, '%')) AND " +
            "(COALESCE(:district, '') = '' OR r.district LIKE CONCAT('%', :district, '%')) AND " +
            "(COALESCE(:ward, '') = '' OR r.ward LIKE CONCAT('%', :ward, '%')) AND " +
            "(:type IS NULL OR r.type = :type) AND " +
            "r.price BETWEEN :minPrice AND :maxPrice AND " +
            "r.maxPeople BETWEEN :minPeople AND :maxPeople")
    List<Room> findByFilter(@Param("city") String city,
                            @Param("district") String district,
                            @Param("ward") String ward,
                            @Param("type") RoomType type,
                            @Param("minPrice") Double minPrice,
                            @Param("maxPrice") Double maxPrice,
                            @Param("minPeople") Integer minPeople,
                            @Param("maxPeople") Integer maxPeople);
}
