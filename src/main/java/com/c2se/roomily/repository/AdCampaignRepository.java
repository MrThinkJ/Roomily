package com.c2se.roomily.repository;

import com.c2se.roomily.entity.AdCampaign;
import com.c2se.roomily.enums.AdCampaignStatus;
import jakarta.persistence.LockModeType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdCampaignRepository extends JpaRepository<AdCampaign, String> {
    List<AdCampaign> findByUserId(String userId);

    @Query("SELECT ac FROM AdCampaign ac WHERE ac.status = 'ACTIVE' AND " +
            "ac.startDate <= :now AND (ac.endDate IS NULL OR ac.endDate >= :now)")
    List<AdCampaign> findActiveCampaigns(@Param("now") LocalDateTime now);
    
    @Query("SELECT ac FROM AdCampaign ac WHERE " +
            "(ac.status = 'DRAFT' AND ac.startDate <= :now) OR " +
            "(ac.status = 'ACTIVE' AND ac.endDate IS NOT NULL AND ac.endDate <= :now) OR " +
            "(ac.status = 'ACTIVE' AND ac.budget <= ac.spentAmount)")
    List<AdCampaign> findCampaignsNeedingStatusUpdate(@Param("now") LocalDateTime now);
    
    List<AdCampaign> findByStatus(AdCampaignStatus status);
    
    List<AdCampaign> findByUserIdAndStatus(String userId, AdCampaignStatus status);

    List<AdCampaign> findByEndDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<AdCampaign> findByStartDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<AdCampaign> findByStatusAndEndDateAfter(AdCampaignStatus status, LocalDateTime endDate);

    @Query("SELECT ac FROM AdCampaign ac WHERE ac.dailySpentAmount > 0 " +
            "AND (ac.status = 'ACTIVE' OR ac.status = 'OUT_OF_DAILY_BUDGET' OR ac.status = 'PAUSED')")
    List<String> findActiveCampaignDailySpentGreaterThanZero();

    @Modifying
    @Query("UPDATE AdCampaign ac SET ac.dailySpentAmount = 0 WHERE ac.id IN (:campaignIds)")
    void resetDailySpentAmount(@Param("campaignIds") List<String> campaignIds);
} 