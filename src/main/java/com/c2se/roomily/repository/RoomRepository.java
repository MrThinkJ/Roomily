package com.c2se.roomily.repository;

import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.Tag;
import com.c2se.roomily.enums.RoomStatus;
import com.c2se.roomily.enums.RoomType;
import com.c2se.roomily.payload.dao.RoomDao;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    List<Room> findByLandlordId(String landlordId);

    @Modifying
    @Query("UPDATE Room r SET r.status = :status WHERE r.id = :roomId")
    void updateRoomStatusById(String roomId, RoomStatus status);

    @Modifying
    @Query("UPDATE Room r SET r.status = :status WHERE r.landlord.id = :landlordId")
    void updateRoomStatusByLandlordId(String landlordId, RoomStatus status);

    @Query(value = "SELECT * FROM room r WHERE " +
            "(COALESCE(:city, '') = '' OR r.city LIKE '%' || :city || '%') AND " +
            "(COALESCE(:district, '') = '' OR r.district LIKE '%' || :district || '%') AND " +
            "(COALESCE(:ward, '') = '' OR r.ward LIKE '%' || :ward || '%') AND " +
            "(:type IS NULL OR r.type = :type) AND " +
            "r.price BETWEEN :minPrice AND :maxPrice AND " +
            "r.max_people BETWEEN :minPeople AND :maxPeople AND " +
            "(r.created_at, r.id) < (:timestamp, :pivotId) " +
            "ORDER BY r.created_at DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Room> findByFilter(@Param("city") String city,
                            @Param("district") String district,
                            @Param("ward") String ward,
                            @Param("type") RoomType type,
                            @Param("minPrice") Double minPrice,
                            @Param("maxPrice") Double maxPrice,
                            @Param("minPeople") Integer minPeople,
                            @Param("maxPeople") Integer maxPeople,
                            @Param("pivotId") String pivotId,
                            @Param("limit") Integer limit,
                            @Param("timestamp") String timestamp
    );

    @Query(value = "SELECT * FROM room r WHERE " +
            "(COALESCE(:city, '') = '' OR r.city LIKE '%' || :city || '%') AND " +
            "(COALESCE(:district, '') = '' OR r.district LIKE '%' || :district || '%') AND " +
            "(COALESCE(:ward, '') = '' OR r.ward LIKE '%' || :ward || '%') AND " +
            "(:type IS NULL OR r.type = :type) AND " +
            "r.price BETWEEN :minPrice AND :maxPrice AND " +
            "r.max_people BETWEEN :minPeople AND :maxPeople" +
            "ORDER BY r.created_at DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Room> findByFilter(@Param("city") String city,
                            @Param("district") String district,
                            @Param("ward") String ward,
                            @Param("type") RoomType type,
                            @Param("minPrice") Double minPrice,
                            @Param("maxPrice") Double maxPrice,
                            @Param("minPeople") Integer minPeople,
                            @Param("maxPeople") Integer maxPeople,
                            @Param("limit") Integer limit
    );

    @Query(value = "SELECT r.* FROM rooms AS r WHERE " +
            "ST_DWithin(" +
            "ST_SetSRID(ST_MakePoint(r.longitude, r.latitude), 4326)::geography," +
            "ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography," +
            ":radius)"
            , nativeQuery = true)
    List<Room> findRoomAround(@Param("lat") Double lat,
                              @Param("lon") Double lon,
                              @Param("radius") Double radius);

    @Query(value = """
            SELECT r.* FROM rooms r
            WHERE r.landlord_id IN :landlordIds
            AND r.room_status = 'AVAILABLE'
            AND ST_DWithin(
                  ST_SetSRID(ST_MakePoint(r.longitude, r.latitude), 4326)::geography,
                  ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                  :radius
                )
            ORDER BY 
                ST_DWithin(
                  ST_SetSRID(ST_MakePoint(r.longitude, r.latitude), 4326)::geography,
                  ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                  :radius
                ) ASC
            """, nativeQuery = true)
    List<Room> findRoomsByLandlordIdsWithinRadius(
            @Param("landlordIds") List<String> landlordIds,
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusKm") double radiusKm
    );

    @Query("SELECT r FROM Room r WHERE r.landlord.id IN :landlordIds " +
            "AND (COALESCE(:city, '') = '' OR r.city LIKE '%' || :city || '%') " +
            "AND (COALESCE(:district, '') = '' OR r.district LIKE '%' || :district || '%') " +
            "AND (COALESCE(:ward, '') = '' OR r.ward LIKE '%' || :ward || '%') " +
            "AND r.status = 'AVAILABLE'")
    List<Room> findRoomsByLandlordIdsAndLocation(
            @Param("landlordIds") List<String> landlordIds,
            @Param("city") String city,
            @Param("district") String district,
            @Param("ward") String ward
    );

    @Query(value = """
                WITH filtered_rooms AS (
                SELECT 
                    r.room_id as id,
                    r.address,
                    r.city,
                    r.description,
                    r.district,
                    r.electric_price as electricPrice,
                    r.max_people as maxPeople,
                    r.nearby_amenities as nearbyAmenities,
                    r.price,
                    r.square_meters as squareMeters,
                    r.title,
                    r.room_type as type,
                    r.ward,
                    r.water_price as waterPrice,
                    CAST(r.created_at AS timestamp) as createdAt,
                    r.deposit,
                    r.room_status as status,
                    CAST(r.updated_at AS timestamp) as updatedAt,
                    r.latitude,
                    r.longitude,
                    r.landlord_id as landlordId,
                    CASE 
                        WHEN :subscribedLandlordIds IS NULL THEN false
                        ELSE (r.landlord_id = ANY(:subscribedLandlordIds)) 
                    END as is_subscribed
                FROM rooms r
                WHERE r.room_status = 'AVAILABLE'
                AND (:type IS NULL OR r.room_type = :type)
                AND r.price BETWEEN :minPrice AND :maxPrice
                AND r.max_people BETWEEN :minPeople AND :maxPeople
                AND (COALESCE(:city, '') = '' OR r.city LIKE '%' || :city || '%')
                AND (COALESCE(:district, '') = '' OR r.district LIKE '%' || :district || '%')
                AND (COALESCE(:ward, '') = '' OR r.ward LIKE '%' || :ward || '%')
                AND (
                    :tagIds IS NULL 
                    OR :tagIds = '{}' 
                    OR EXISTS (
                        SELECT 1 
                        FROM room_tags rt 
                        WHERE rt.room_id = r.room_id 
                        AND rt.tag_id = ANY(:tagIds)
                    )
                )
            )
            SELECT *
            FROM filtered_rooms fr
            WHERE
            (
                fr.is_subscribed = :pivotSubscribed
                AND (fr.createdAt, fr.id) < (:timestamp, :pivotId)
            )
            OR (
                fr.is_subscribed > :pivotSubscribed
                AND (fr.createdAt, fr.id) < (:timestamp, :pivotId)
            )
            ORDER BY 
                fr.is_subscribed DESC,
                fr.createdAt DESC, 
                fr.id DESC
            LIMIT :limit
                """,
            nativeQuery = true)
    List<RoomDao> findRoomsWithCursor(
            @Param("city") String city,
            @Param("district") String district,
            @Param("ward") String ward,
            @Param("type") String type,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minPeople") Integer minPeople,
            @Param("maxPeople") Integer maxPeople,
            @Param("subscribedLandlordIds") String[] subscribedLandlordIds,
            @Param("pivotSubscribed") boolean pivotSubscribed,
            @Param("timestamp") LocalDateTime timestamp,
            @Param("pivotId") String pivotId,
            @Param("limit") int limit,
            @Param("tagIds") String[] tagIds
    );

    boolean existsById(@NotNull String roomId);
    @Query("SELECT r.tags FROM Room r WHERE r.id = :roomId")
    Set<Tag> findTagsById(String roomId);
}
