package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdImpressionRequest {
    private List<String> promotedRoomIds;
    private String userId;
}
