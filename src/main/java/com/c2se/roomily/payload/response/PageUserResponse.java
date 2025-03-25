package com.c2se.roomily.payload.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PageUserResponse {
    private List<UserResponse> users;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isFirst;
    private boolean isLast;
}
