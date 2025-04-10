package com.c2se.roomily.repository;

import com.c2se.roomily.entity.LandlordInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LandlordInfoRepository extends JpaRepository<LandlordInfo, String> {
    Optional<LandlordInfo> findByUserId(String userId);
    boolean existsByUserId(String userId);
} 