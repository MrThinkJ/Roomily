package com.c2se.roomily.repository;

import com.c2se.roomily.entity.LandlordReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LandlordReviewRepository extends JpaRepository<LandlordReview, String> {
} 