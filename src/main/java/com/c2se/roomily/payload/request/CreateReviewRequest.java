package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateReviewRequest {
    private String content;
    private int rating;
}
