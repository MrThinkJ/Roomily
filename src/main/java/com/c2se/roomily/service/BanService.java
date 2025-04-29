package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.BanUserRequest;
import com.c2se.roomily.payload.response.BanHistoryResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface BanService {
    void banUser(BanUserRequest banUserRequest);

    void unbanUser(String userId);

    Boolean isUserBanned(String userId);

    List<BanHistoryResponse> getUserBanHistory(String userId, Integer page, Integer size);

    List<BanHistoryResponse> getAllActiveBans();
    
    void processExpiredBans();
} 