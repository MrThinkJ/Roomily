package com.c2se.roomily.repository;

import com.c2se.roomily.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findByUserId(String userId);

    List<Review> findByRoomId(String roomId);
} 