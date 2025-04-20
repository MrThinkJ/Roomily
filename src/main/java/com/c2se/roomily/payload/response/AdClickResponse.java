package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdClickResponse {
    private String adClickId;
    private String status;
}
