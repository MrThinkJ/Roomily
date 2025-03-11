package com.c2se.roomily.repository;

import com.c2se.roomily.entity.FindPartnerPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FindPartnerPostRepository extends JpaRepository<FindPartnerPost, String> {
    boolean existsByPosterId(String posterId);
    @Query(value = "SELECT fp FROM FindPartnerPost fp WHERE fp.room.id = ?1")
    List<FindPartnerPost> findByRoomId(String roomId);
}
