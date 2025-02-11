package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserReportSummary {
    private String userId;
    private String username;
    private Long reportCount;
}
