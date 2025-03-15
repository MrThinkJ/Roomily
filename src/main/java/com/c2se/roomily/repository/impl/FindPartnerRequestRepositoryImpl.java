package com.c2se.roomily.repository.impl;

import com.c2se.roomily.repository.FindPartnerRequestRepository;
import com.c2se.roomily.util.UtilFunction;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@AllArgsConstructor
public class FindPartnerRequestRepositoryImpl implements FindPartnerRequestRepository {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void save(String key, String value, int ttl) {
        String keyWithPrefix = "find_partner:" + key;
        redisTemplate.opsForValue().set(keyWithPrefix, value, ttl, TimeUnit.MINUTES);
    }

    @Override
    public String findByKey(String key) {
        String keyWithPrefix = "find_partner:" + key;
        return redisTemplate.opsForValue().get(keyWithPrefix);
    }

    @Override
    public void deleteByKey(String key) {
        redisTemplate.delete("find_partner:" + key);
    }

    @Override
    public String generateKey(String userId, String findPartnerPostId, String chatRoomId, int ttl) {
        String value = String.format("%s#%s#%s", userId, findPartnerPostId, chatRoomId);
        String privateCode = UtilFunction.hash(value);
        save(privateCode, value, ttl);
        return privateCode;
    }
}
