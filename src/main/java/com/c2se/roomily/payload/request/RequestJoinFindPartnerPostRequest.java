package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestJoinFindPartnerPostRequest {
    private String findPartnerPostId;
    private String chatRoomId;
    private String userId;
}
