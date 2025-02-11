package com.c2se.roomily.repository;

import com.c2se.roomily.entity.BanHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BanHistoryRepository extends JpaRepository<BanHistory, String> {
    @Query("SELECT b FROM BanHistory b WHERE b.user.id = :userId AND " +
            "b.expiresAt > CURRENT_TIMESTAMP")
    Optional<BanHistory> findActiveBanByUserId(String userId);
    Page<BanHistory> findByUserId(String userId, Pageable pageable);
    List<BanHistory> findByExpiresAtBefore(LocalDateTime expiresAt);
}
