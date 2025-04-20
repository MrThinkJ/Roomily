package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdClickRequest {
    private String promotedRoomId;
    private String ipAddress;
    private String userId;
}
