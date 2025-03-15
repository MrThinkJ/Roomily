package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.RentalRequest;

import java.util.Optional;

public interface RequestCacheService {
    RentalRequest saveRequest(RentalRequest request);

    Optional<RentalRequest> getRequest(String requestId);

    void removeRequest(String requestId);
}
