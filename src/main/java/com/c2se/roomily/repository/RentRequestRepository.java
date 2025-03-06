package com.c2se.roomily.repository;

import com.c2se.roomily.payload.request.CreateRentRequest;

public interface RentRequestRepository extends RedisRepository<String, String> {
    String generateKey(String userId, CreateRentRequest createRentRequest, int ttl);
}
