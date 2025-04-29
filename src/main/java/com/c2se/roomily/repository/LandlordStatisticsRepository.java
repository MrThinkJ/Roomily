package com.c2se.roomily.repository;

import com.c2se.roomily.entity.LandlordStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LandlordStatisticsRepository extends JpaRepository<LandlordStatistics, String> {
    Optional<LandlordStatistics> findByLandlordId(String landlordId);
} 