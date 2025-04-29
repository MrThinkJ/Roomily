package com.c2se.roomily.repository.impl;

import com.c2se.roomily.payload.request.AdClickRequest;
import com.c2se.roomily.repository.AdsClickDeDupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class AdsClickDeDupRepositoryImpl implements AdsClickDeDupRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private final String AD_CLICK_KEY_PREFIX = "click_dedup:";
    private final int TTL = 15;
    @Override
    public boolean save(AdClickRequest adClickRequest) {
        String key = adClickRequest.getUserId() + ":"
                + adClickRequest.getIpAddress() + ":"
                + adClickRequest.getPromotedRoomId();
        String redisKey = AD_CLICK_KEY_PREFIX + key;
        if (!redisTemplate.hasKey(redisKey)) {
            redisTemplate.opsForValue().set(redisKey, "1", TTL, TimeUnit.MINUTES);
            return true;
        }
        return false;
    }

    @Override
    public void deleteById(String id) {

    }
}
