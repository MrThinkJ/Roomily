package com.c2se.roomily.repository;

import com.c2se.roomily.entity.WithdrawInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WithdrawInfoRepository extends JpaRepository<WithdrawInfo, String> {
    Optional<WithdrawInfo> findByUserId(String userId);
    void deleteByUserId(String userId);
}
