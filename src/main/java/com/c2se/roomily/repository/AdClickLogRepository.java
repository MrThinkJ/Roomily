package com.c2se.roomily.repository;

import com.c2se.roomily.entity.AdClickLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdClickLogRepository extends JpaRepository<AdClickLog, String>{
}
