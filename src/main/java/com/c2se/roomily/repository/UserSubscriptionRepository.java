package com.c2se.roomily.repository;

import com.c2se.roomily.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, String> {
    boolean existsByUserIdAndSubscriptionIdAndEndDateAfter(String userId, String subscriptionId, LocalDateTime endDate);
    List<UserSubscription> findByUserId(String userId);
    List<UserSubscription> findByEndDateBetweenAndAutoRenewIsTrue(LocalDateTime startDate, LocalDateTime endDate);
    Optional<UserSubscription> findByUserIdAndSubscriptionId(String userId, String subscriptionId);
    Optional<UserSubscription> findByUserIdAndEndDateAfter(String userId, LocalDateTime endDate);
    Optional<UserSubscription> findByEndDateAfter(LocalDateTime endDate);

    @Query("SELECT us.subscription.id FROM UserSubscription us " +
           "WHERE us.endDate > :now " +
           "GROUP BY us.subscription.id " +
           "ORDER BY COUNT(us) DESC " +
           "LIMIT 1")
    Optional<String> findMostPopularActiveSubscriptionId(@Param("now") LocalDateTime now);
}