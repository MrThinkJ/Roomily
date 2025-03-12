package com.c2se.roomily.repository;

import com.c2se.roomily.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByUserId(String userId);

    List<Notification> findByUserIdAndIsRead(String userId, Boolean isRead);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId")
    void markAllNotificationsAsRead(@Param("userId") String userId);
} 