package com.c2se.roomily.repository;

import com.c2se.roomily.entity.FindPartnerPost;
import com.c2se.roomily.enums.FindPartnerPostStatus;
import com.c2se.roomily.enums.FindPartnerPostType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FindPartnerPostRepository extends JpaRepository<FindPartnerPost, String> {
    boolean existsByPosterIdAndStatus(String posterId, FindPartnerPostStatus findPartnerPostStatus);

    Optional<FindPartnerPost> findByRoomIdAndType(String roomId, FindPartnerPostType type);

    @Query(value = "SELECT fp FROM FindPartnerPost fp WHERE fp.room.id = ?1")
    List<FindPartnerPost> findByRoomId(String roomId);

    List<FindPartnerPost> findByRoomIdAndStatus(String roomId, FindPartnerPostStatus status);

    @Query(value = "SELECT fp.id FROM FindPartnerPost fp WHERE fp.room.id = :roomId AND fp.status = :status")
    List<String> findIdsByRoomIdAndStatus(String roomId, FindPartnerPostStatus status);

    @Query(value = "SELECT fp FROM FindPartnerPost fp WHERE :userId IN (SELECT p.id FROM fp.participants p) AND fp.status = :status")
    List<FindPartnerPost> findActiveByUserIdInParticipants(@Param("userId") String userId,
                                                         @Param("status") FindPartnerPostStatus status);

    boolean existsByRentedRoomIdAndType(String rentedRoomId, FindPartnerPostType type);

    @Query(value = "SELECT COUNT(fp) > 0 FROM FindPartnerPost fp WHERE fp.room.id = :roomId AND fp.status = 'ACTIVE'")
    boolean hasActiveFindPartnerPostByRoomId(String roomId);
    
    @Query(value = "SELECT COUNT(p) > 0 FROM FindPartnerPost fp JOIN fp.participants p WHERE fp.id = :postId AND p.id = :userId")
    boolean existsByPostIdAndParticipantId(@Param("postId") String postId, @Param("userId") String userId);
    
    @Query(value = "INSERT INTO find_partner_participants (find_partner_post_id, user_id) " +
           "SELECT :postId, :userId WHERE NOT EXISTS " +
           "(SELECT 1 FROM find_partner_participants WHERE find_partner_post_id = :postId AND user_id = :userId)",
           nativeQuery = true)
    @Modifying
    @Transactional
    void addParticipantIfNotExists(@Param("postId") String postId, @Param("userId") String userId);
}
