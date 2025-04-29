package com.c2se.roomily.repository;

import com.c2se.roomily.entity.PromotedRoom;
import com.c2se.roomily.enums.PromotedRoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotedRoomRepository extends JpaRepository<PromotedRoom, String> {
    List<PromotedRoom> findByAdCampaignId(String campaignId);

    @Query("SELECT pr FROM PromotedRoom pr WHERE pr.status = 'ACTIVE' AND " +
            "pr.adCampaign.status = 'ACTIVE' AND " +
            "pr.adCampaign.startDate <= :now AND " +
            "(pr.adCampaign.endDate IS NULL OR pr.adCampaign.endDate >= :now)")
    List<PromotedRoom> findActivePromotedRooms(@Param("now") LocalDateTime now);

    @Query("SELECT pr FROM PromotedRoom pr WHERE pr.status = 'ACTIVE' AND " +
            "pr.adCampaign.id = :campaignId")
    List<PromotedRoom> findActivePromotedRoomsByCampaignId(@Param("campaignId") String campaignId);

    @Query("SELECT pr FROM PromotedRoom pr WHERE pr.status = 'ACTIVE' AND pr.room.id = :roomId AND " +
            "pr.adCampaign.status = 'ACTIVE' AND " +
            "pr.adCampaign.startDate <= :now AND " +
            "(pr.adCampaign.endDate IS NULL OR pr.adCampaign.endDate >= :now)")
    Optional<PromotedRoom> findActivePromotedRoomByRoomId(@Param("roomId") String roomId, @Param("now") LocalDateTime now);

    @Query("SELECT pr FROM PromotedRoom pr WHERE pr.status = 'ACTIVE' AND pr.adCampaign.user.id = :userId AND " +
            "pr.adCampaign.status = 'ACTIVE' AND " +
            "pr.adCampaign.startDate <= :now AND " +
            "(pr.adCampaign.endDate IS NULL OR pr.adCampaign.endDate >= :now)")
    List<PromotedRoom> findActiveByUserId(String userId);
    
    List<PromotedRoom> findByStatus(PromotedRoomStatus status);
    
    List<PromotedRoom> findByRoomId(String roomId);

    @Modifying
    @Query("UPDATE PromotedRoom pr SET pr.status = :status WHERE pr.adCampaign.id = :campaignId")
    void updateStatusByCampaignId(PromotedRoomStatus status, String campaignId);

    @Modifying
    @Query("UPDATE PromotedRoom pr SET pr.status = :status WHERE pr.id IN :ids")
    void updateStatusByIds(PromotedRoomStatus status, List<String> ids);
}
