package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateRoomReportRequest {
    private String reporterId;
    private String roomId;
    private String reason;
}
