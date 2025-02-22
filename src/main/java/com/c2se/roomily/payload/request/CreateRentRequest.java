package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CreateRentRequest {
    private String userId;
    private String roomId;
    private String landlordId;
    private String startDate;
    private String endDate;
    private String privateCode;
    private LocalDateTime createdAt;
}
