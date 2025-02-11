package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BanHistoryResponse {
    private String id;
    private String userId;
    private String reason;
    private String bannedAt;
    private String expiresAt;
    private String unbannedAt;
} 