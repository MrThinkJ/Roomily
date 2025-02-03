package com.c2se.roomily.repository;

import com.c2se.roomily.entity.PricingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PricingHistoryRepository extends JpaRepository<PricingHistory, String> {
} 