package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserReportResponse {
    private String id;
    private String type;
    private String content;
    private String createdAt;
    private String status;
    private String reportedUserId;
    private String reporterId;
}
