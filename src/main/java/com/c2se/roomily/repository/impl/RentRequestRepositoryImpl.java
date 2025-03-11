package com.c2se.roomily.repository.impl;

import com.c2se.roomily.payload.request.CreateRentRequest;
import com.c2se.roomily.repository.RentRequestRepository;
import com.c2se.roomily.util.UtilFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RentRequestRepositoryImpl implements RentRequestRepository {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public String generateKey(String userId, CreateRentRequest createRentRequest, int ttl) {
        String value = serializeRequest(createRentRequest);
        String privateCode = UtilFunction.generatePrivateCode(value);
        save(privateCode, value, ttl);
        return privateCode;
    }

    @Override
    public void save(String key, String value, int ttl) {
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, ttl, TimeUnit.MINUTES);
    }

    @Override
    public String findByKey(String key) {
        return redisTemplate.opsForValue().get("rent_request:" + key);
    }

    @Override
    public void deleteByKey(String key) {
        redisTemplate.delete("rent_request:" + key);
    }

    private String serializeRequest(CreateRentRequest createRentRequest) {
        return createRentRequest.getUserId() +
                "#" + createRentRequest.getRoomId() +
                "#" + createRentRequest.getStartDate() +
                "#" + createRentRequest.getFindPartnerPostId();
    }
}
