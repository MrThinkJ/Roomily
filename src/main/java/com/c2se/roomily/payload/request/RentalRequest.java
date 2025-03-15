package com.c2se.roomily.payload.request;

import com.c2se.roomily.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RentalRequest {
    private String id;
    private String requesterId;
    private String recipientId;
    private String findPartnerPostId;
    private RequestStatus status;
    private String expiresAt;
}
