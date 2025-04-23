package com.c2se.roomily.repository.impl;

import com.c2se.roomily.payload.response.CheckoutResponse;
import com.c2se.roomily.repository.CheckoutInfoRepository;
import com.c2se.roomily.util.UtilFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CheckoutInfoRepositoryImpl implements CheckoutInfoRepository{
    private static final int REQUEST_TTL = 60*12;
    private static final String REQUEST_KEY_PREFIX = "checkout:";
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public CheckoutResponse save(String checkoutId, CheckoutResponse checkoutResponse) {
        checkoutResponse.setId(checkoutId);
        checkoutResponse.setExpiresAt(LocalDateTime.now().plusMinutes(REQUEST_TTL).toString());
        redisTemplate.opsForValue().set(
                REQUEST_KEY_PREFIX+checkoutId, checkoutResponse, REQUEST_TTL, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(
                "persistent:checkout:" + checkoutId, checkoutResponse);
        return checkoutResponse;
    }

    @Override
    public Optional<CheckoutResponse> findById(String checkoutId) {
        Object value = redisTemplate.opsForValue()
                .get(REQUEST_KEY_PREFIX + checkoutId);
        if (value == null) {
            return Optional.empty();
        }
        CheckoutResponse checkoutResponse = (CheckoutResponse) value;
        return Optional.of(checkoutResponse);
    }

    @Override
    public void delete(String checkoutId) {
        redisTemplate.delete(REQUEST_KEY_PREFIX + checkoutId);
    }
}
