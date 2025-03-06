package com.c2se.roomily.repository;

import com.c2se.roomily.entity.LandlordReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LandlordReviewRepository extends JpaRepository<LandlordReview, String> {
    List<LandlordReview> findByLandlordId(String landlordId);

    List<LandlordReview> findByReviewerId(String reviewerId);

    List<LandlordReview> findByLandlordIdAndReviewerId(String landlordId, String reviewerId);
} 