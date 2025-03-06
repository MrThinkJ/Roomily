package com.c2se.roomily.repository;

import com.c2se.roomily.entity.FindPartnerPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FindPartnerPostRepository extends JpaRepository<FindPartnerPost, String> {
    boolean existsByPosterId(String posterId);
}
