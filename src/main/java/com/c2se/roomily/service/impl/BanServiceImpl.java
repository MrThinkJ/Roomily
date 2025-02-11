package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.BanHistory;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.UserStatus;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.response.BanHistoryResponse;
import com.c2se.roomily.repository.BanHistoryRepository;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.service.BanService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BanServiceImpl implements BanService {
    UserRepository userRepository;
    BanHistoryRepository banHistoryRepository;

    @Override
    public Boolean banUser(String userId, String reason, LocalDateTime expiresAt) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        BanHistory activeBan = banHistoryRepository.findActiveBanByUserId(userId).orElse(
                BanHistory.builder()
                        .user(user)
                        .reason(reason)
                        .bannedAt(LocalDateTime.now())
                        .expiresAt(expiresAt)
                        .build()
        );
        user.setStatus(UserStatus.BANNED);
        userRepository.save(user);
        banHistoryRepository.save(activeBan);
        return true;
    }

    @Override
    public Boolean unbanUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        BanHistory activeBan = banHistoryRepository.findActiveBanByUserId(userId).orElse(null);
        if (activeBan != null) {
            activeBan.setExpiresAt(LocalDateTime.now());
            banHistoryRepository.save(activeBan);
        }
        return true;
    }

    @Override
    public Boolean isUserBanned(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
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
                .bannedAt(banHistory.getBannedAt().toString())
                .expiresAt(banHistory.getExpiresAt() != null ? 
                        banHistory.getExpiresAt().toString() : null)
                .build();
    }
} 