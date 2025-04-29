package com.c2se.roomily.repository;

import com.c2se.roomily.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, String> {
    List<UserDevice> findByUserIdAndIsActiveTrue(String userId);
    List<UserDevice> findByUserId(String userId);
    Optional<UserDevice> findByFcmToken(String fcmToken);
    UserDevice findByFcmTokenAndUserId(String fcmToken, String userId);
    boolean existsByFcmTokenAndIsActiveTrue(String fcmToken);
}
