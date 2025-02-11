package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserReportRequest {
    private String reportedUserId;
    private String reporterId;
    private String type;
    private String content;
}
