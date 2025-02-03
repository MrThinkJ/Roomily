package com.c2se.roomily.repository;

import com.c2se.roomily.entity.BillLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillLogRepository extends JpaRepository<BillLog, String> {
} 