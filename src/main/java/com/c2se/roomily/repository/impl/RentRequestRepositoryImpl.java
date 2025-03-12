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
        String keyPersisted = "rent_request:" + key;
        redisTemplate.opsForValue().set(keyPersisted, value, ttl, TimeUnit.MINUTES);
    }

    @Override
    public String findByKey(String key) {
        String keyPersisted = "rent_request:" + key;
        return redisTemplate.opsForValue().get(keyPersisted);
    }

    @Override
    public void deleteByKey(String key) {
        redisTemplate.delete("rent_request:" + key);
    }

    private String serializeRequest(CreateRentRequest createRentRequest) {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder
                .append(createRentRequest.getUserId())
                .append("#")
                .append(createRentRequest.getRoomId())
                .append("#")
                .append(createRentRequest.getStartDate())
                .append("#")
                .append(createRentRequest.getFindPartnerPostId())
                .append("#")
                .append(createRentRequest.getChatRoomId())
                .toString();
    }
}
