package com.c2se.roomily.repository.impl;

import com.c2se.roomily.repository.AdsConversionDeDupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class AdsConversionDeDupRepositoryImpl implements AdsConversionDeDupRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private final String ADS_CONVERSION_KEY_PREFIX = "conversion_dedup:";
    private final int TTL = 15;
    @Override
    public boolean save(String userId, String chatRoomId) {
        String key = userId + ":" + chatRoomId;
        String redisKey = ADS_CONVERSION_KEY_PREFIX + key;
        if (!redisTemplate.hasKey(redisKey)) {
            redisTemplate.opsForValue().set(redisKey, "1", TTL, TimeUnit.MINUTES);
            return true;
        }
        return false;
    }

    @Override
    public void deleteById(String id) {
        String redisKey = ADS_CONVERSION_KEY_PREFIX + id;
        if (redisTemplate.hasKey(redisKey)) {
            redisTemplate.delete(redisKey);
        }
    }
}
