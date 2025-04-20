package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class AdImpressionResponse {
    private String adImpressionId;
    private String status;
    private String timestamp;
}
