package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateLandlordReviewRequest {
    private Integer rating;
    private String content;
}
