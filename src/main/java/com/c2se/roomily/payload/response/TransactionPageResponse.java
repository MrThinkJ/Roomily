package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TransactionPageResponse {
    private PageResponse page;
    private List<TransactionResponse> content;
}
