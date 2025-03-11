package com.c2se.roomily.repository;

import com.c2se.roomily.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, String> {
    boolean existsByUserIdAndRoomId(String userId, String roomId);

    Optional<Favorite> findByUserIdAndRoomId(String userId, String roomId);

    @Modifying
    @Query("UPDATE Favorite f SET f.isFavorite = :isFavorite WHERE f.user.id = :userId AND f.room.id = :roomId")
    void toggleByUserIdAndRoomId(@Param(":userId") String userId,
                                 @Param(":userId") String roomId,
                                 @Param("isFavorite") boolean isFavorite);

    void deleteByUserIdAndRoomId(String userId, String roomId);

    List<Favorite> findAllByUserId(String userId);

    @Query("SELECT f FROM Favorite f WHERE f.user.id = :userId AND f.isFavorite = true")
    List<Favorite> findByUserIdAndFavoriteIsTrue(String userId);

    int countByUserId(String userId);

    int countByRoomId(String roomId);
} 