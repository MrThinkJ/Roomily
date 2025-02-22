package com.c2se.roomily.repository.impl;

import com.c2se.roomily.payload.request.CreateRentRequest;
import com.c2se.roomily.repository.RentRequestRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class RentRequestRepositoryImpl implements RentRequestRepository {

    @Override
    public String save(String userId, CreateRentRequest createRentRequest) {
        String key = generateKey(userId, createRentRequest.getPrivateCode());
        // TODO: save to redis
//        redisTemplate.opsForValue().set(key, request);
//        redisTemplate.expire(key, REQUEST_TTL, TimeUnit.HOURS);
        return key;
    }

    @Override
    public CreateRentRequest findByUserId(String userId) {
//        return redisTemplate.opsForValue().get("rent_request:" + userId);
        return null;
    }

    private String generateKey(String userId, String code) {
        return String.format("rent_request:%s:%s", userId, code);
    }
}
