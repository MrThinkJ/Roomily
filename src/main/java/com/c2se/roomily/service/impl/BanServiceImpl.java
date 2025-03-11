package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.BanHistory;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.RoomStatus;
import com.c2se.roomily.enums.UserStatus;
import com.c2se.roomily.payload.response.BanHistoryResponse;
import com.c2se.roomily.repository.BanHistoryRepository;
import com.c2se.roomily.service.BanService;
import com.c2se.roomily.service.RoomService;
import com.c2se.roomily.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BanServiceImpl implements BanService {
    UserService userService;
    BanHistoryRepository banHistoryRepository;
    RoomService roomService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean banUser(String userId, String reason, LocalDateTime expiresAt) {
        User user = userService.getUserEntity(userId);
        if (banHistoryRepository.existsActiveBanByUserId(userId)) {
            return false;
        }
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Ban expiration must be in the future");
        }
        BanHistory activeBan = BanHistory.builder()
                .user(user)
                .reason(reason)
                .bannedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();
        banHistoryRepository.save(activeBan);
        userService.updateUserStatus(user, UserStatus.BANNED);
        roomService.updateRoomStatusByLandlordId(userId, RoomStatus.BANNED);
        return true;
    }

    @Override
    public Boolean unbanUser(String userId) {
        User user = userService.getUserEntity(userId);
        BanHistory activeBan = banHistoryRepository.findActiveBanByUserId(userId).orElse(null);
        if (activeBan == null)
            return false;
        activeBan.setExpiresAt(LocalDateTime.now());
        banHistoryRepository.save(activeBan);
        userService.updateUserStatus(user, UserStatus.ACTIVE);
        roomService.updateRoomStatusByLandlordId(userId, RoomStatus.AVAILABLE);
        return true;
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
        List<BanHistory> activeBans = banHistoryRepository.findByExpiresAtBefore(LocalDateTime.now());
        return activeBans.stream()
                .map(this::mapToBanHistoryResponse)
                .collect(Collectors.toList());
    }

    private BanHistoryResponse mapToBanHistoryResponse(BanHistory banHistory) {
        return BanHistoryResponse.builder()
                .id(banHistory.getId())
                .userId(banHistory.getUser().getId())
                .reason(banHistory.getReason())
                .bannedAt(banHistory.getBannedAt())
                .expiresAt(banHistory.getExpiresAt() != null ?
                                   banHistory.getExpiresAt() : null)
                .build();
    }
} 