package com.c2se.roomily.entity;

import com.c2se.roomily.enums.DeviceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_devices")
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
@Builder
public class UserDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_device_id")
    private String id;
    @Column(name="fcm_token", nullable = false)
    private String fcmToken;
    @Column(name="device_type")
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
