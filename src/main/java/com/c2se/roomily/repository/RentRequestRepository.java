package com.c2se.roomily.repository;

import com.c2se.roomily.payload.request.CreateRentRequest;

public interface RentRequestRepository {
    String save(String userId, CreateRentRequest createRentRequest);
    CreateRentRequest findByUserId(String userId);
}
