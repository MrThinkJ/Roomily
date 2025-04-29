package com.c2se.roomily.repository;

import com.c2se.roomily.entity.AdImpressionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdsImpressionLogRepository extends JpaRepository<AdImpressionLog, String> {
    List<AdImpressionLog> findByIsProcessedFalse();

    @Modifying
    @Query("UPDATE AdImpressionLog a SET a.isProcessed = true WHERE a.id IN :impressionIds")
    void markAsProcessed(List<String> impressionIds);
}
