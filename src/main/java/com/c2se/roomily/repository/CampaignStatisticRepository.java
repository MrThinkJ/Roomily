package com.c2se.roomily.repository;

import com.c2se.roomily.entity.CampaignStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignStatisticRepository extends JpaRepository<CampaignStatistic, String> {
//    @Query("SELECT SUM(cs.impressions) FROM CampaignStatistic cs " +
//            "WHERE cs.adCampaign.id = :campaignId")
//    Long getTotalImpressionsByCampaignId(@Param("campaignId") String campaignId);
    
    @Query("SELECT SUM(cs.clicks) FROM CampaignStatistic cs " +
            "WHERE cs.adCampaign.id = :campaignId")
    Long getTotalClicksByCampaignId(@Param("campaignId") String campaignId);
    
    @Query("SELECT SUM(cs.conversionCount) FROM CampaignStatistic cs " +
            "WHERE cs.adCampaign.id = :campaignId")
    Long getTotalConversionsByCampaignId(@Param("campaignId") String campaignId);
    Optional<CampaignStatistic> findByAdCampaignId(String campaignId);
} 