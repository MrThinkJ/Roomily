package com.c2se.roomily.repository;

import com.c2se.roomily.entity.FindPartnerPost;
import com.c2se.roomily.enums.FindPartnerPostStatus;
import com.c2se.roomily.enums.FindPartnerPostType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FindPartnerPostRepository extends JpaRepository<FindPartnerPost, String> {
    boolean existsByPosterId(String posterId);

    Optional<FindPartnerPost> findByRoomIdAndType(String roomId, FindPartnerPostType type);

    @Query(value = "SELECT fp FROM FindPartnerPost fp WHERE fp.room.id = ?1")
    List<FindPartnerPost> findByRoomId(String roomId);

    List<FindPartnerPost> findByRoomIdAndStatus(String roomId, FindPartnerPostStatus status);

    @Query(value = "SELECT fp FROM FindPartnerPost fp WHERE :userId IN (SELECT p.id FROM fp.participants p) AND fp.status = :status")
    List<FindPartnerPost> findActiveByUserIdInParticipants(@Param("userId") String userId,
                                                         @Param("status") FindPartnerPostStatus status);

    boolean existsByRentedRoomIdAndType(String rentedRoomId, FindPartnerPostType type);
}
