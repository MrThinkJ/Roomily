package com.c2se.roomily.repository;

import com.c2se.roomily.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, String> {
    boolean existsByUserIdAndRoomId(String userId, String roomId);

    @Modifying
    @Query("update Favorite f set f.isFavorite = not(f.isFavorite) where f.room = :roomId and f.user = :userId")
    void toggleByUserIdAndRoomId(@Param(":userId") String userId,
                                 @Param(":userId") String roomId);

    void deleteByUserIdAndRoomId(String userId, String roomId);

    List<Favorite> findAllByUserId(String userId);

    int countByUserId(String userId);

    int countByRoomId(String roomId);
} 