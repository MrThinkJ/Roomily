package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.BanHistory;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.RoomStatus;
import com.c2se.roomily.enums.UserStatus;
import com.c2se.roomily.payload.request.BanUserRequest;
import com.c2se.roomily.payload.response.BanHistoryResponse;
import com.c2se.roomily.repository.BanHistoryRepository;
import com.c2se.roomily.service.BanService;
import com.c2se.roomily.service.RoomService;
import com.c2se.roomily.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class BanServiceImpl implements BanService {
    UserService userService;
    BanHistoryRepository banHistoryRepository;
    RoomService roomService;
    TaskScheduler taskScheduler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banUser(BanUserRequest banUserRequest) {
        User user = userService.getUserEntity(banUserRequest.getUserId());
        if (banHistoryRepository.existsActiveBanByUserId(banUserRequest.getUserId())) {
            return;
        }
        if (banUserRequest.getExpiresAt() != null && banUserRequest.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Ban expiration must be in the future");
        }
        BanHistory activeBan = BanHistory.builder()
                .user(user)
                .reason(banUserRequest.getReason())
                .bannedAt(LocalDateTime.now())
                .expiresAt(banUserRequest.getExpiresAt())
                .build();
        banHistoryRepository.save(activeBan);
        userService.updateUserStatus(user, UserStatus.BANNED);
        roomService.updateRoomStatusByLandlordId(banUserRequest.getUserId(), RoomStatus.BANNED);
        
        // Schedule unban task if expiration is set
        if (banUserRequest.getExpiresAt() != null) {
            scheduleUnban(activeBan.getId(), activeBan.getUser().getId(), activeBan.getExpiresAt());
        }
    }

    @Override
    public void unbanUser(String userId) {
        User user = userService.getUserEntity(userId);
        BanHistory activeBan = banHistoryRepository.findActiveBanByUserId(userId).orElse(null);
        if (activeBan == null)
            return;
        activeBan.setExpiresAt(LocalDateTime.now());
        banHistoryRepository.save(activeBan);
        userService.updateUserStatus(user, UserStatus.ACTIVE);
        roomService.updateRoomStatusByLandlordId(userId, RoomStatus.AVAILABLE);
    }

    @Override
    public Boolean isUserBanned(String userId) {
        User user = userService.getUserEntity(userId);
        return UserStatus.BANNED.equals(user.getStatus());
    }

    @Override
    public List<BanHistoryResponse> getUserBanHistory(String userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BanHistory> banHistory = banHistoryRepository.findByUserId(userId, pageable);
        return banHistory.getContent().stream()
                .map(this::mapToBanHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BanHistoryResponse> getAllActiveBans() {
        List<BanHistory> activeBans = banHistoryRepository.findByExpiresAtAfter(LocalDateTime.now());
        return activeBans.stream()
                .map(this::mapToBanHistoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    @Transactional(readOnly = true)
    public void processExpiredBans() {
        log.info("Scheduling unban tasks for bans expiring today");
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        List<BanHistory> bansExpiringToday = banHistoryRepository.findBansExpiringToday(startOfDay, endOfDay);
        
        if (bansExpiringToday.isEmpty()) {
            log.info("No bans expiring today");
            return;
        }
        
        log.info("Found {} bans expiring today", bansExpiringToday.size());
        
        for (BanHistory ban : bansExpiringToday) {
            try {
                String userId = ban.getUser().getId();
                String banId = ban.getId();
                LocalDateTime expiryTime = ban.getExpiresAt();
                
                log.info("Scheduling unban for user ID: {} at {}", userId, expiryTime);
                
                // Schedule the unban at the exact expiry time
                scheduleUnban(banId, userId, expiryTime);
                
            } catch (Exception e) {
                log.error("Error scheduling unban for user: {}", ban.getUser().getId(), e);
            }
        }
        
        log.info("Completed scheduling unbans for today");
    }
    
    private void scheduleUnban(String banId, String userId, LocalDateTime expiryTime) {
        Runnable unbanTask = () -> processUnban(banId, userId);
        taskScheduler.schedule(unbanTask, expiryTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    @Transactional(rollbackFor = Exception.class)
    protected void processUnban(String banId, String userId) {
        try {
            log.info("Processing scheduled unban for user ID: {}", userId);
            
            BanHistory ban = banHistoryRepository.findById(banId).orElse(null);
            if (ban == null) {
                log.warn("Ban not found for ID: {}", banId);
                return;
            }
            
            User user = userService.getUserEntity(userId);
            if (!UserStatus.BANNED.equals(user.getStatus())) {
                log.info("User {} is not currently banned, no action needed", userId);
                return;
            }
            
            // Unban the user
            userService.updateUserStatus(user, UserStatus.ACTIVE);
            roomService.updateRoomStatusByLandlordId(userId, RoomStatus.AVAILABLE);
            
            log.info("Successfully unbanned user with ID: {}", userId);
        } catch (Exception e) {
            log.error("Error processing unban for user: {}", userId, e);
        }
    }

    private BanHistoryResponse mapToBanHistoryResponse(BanHistory banHistory) {
        return BanHistoryResponse.builder()
                .id(banHistory.getId())
                .userId(banHistory.getUser().getId())
                .username(banHistory.getUser().getUsername())
                .role(banHistory.getUser().getRoles().stream().toList().get(0).getName())
                .reason(banHistory.getReason())
                .bannedAt(banHistory.getBannedAt())
                .expiresAt(banHistory.getExpiresAt() != null ?
                                   banHistory.getExpiresAt() : null)
                .build();
    }
} 