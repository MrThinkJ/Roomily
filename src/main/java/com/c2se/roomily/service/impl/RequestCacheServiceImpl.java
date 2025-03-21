package com.c2se.roomily.service.impl;

import com.c2se.roomily.payload.request.RentalRequest;
import com.c2se.roomily.service.RequestCacheService;
import com.c2se.roomily.util.UtilFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestCacheServiceImpl implements RequestCacheService {
    private static final int REQUEST_TTL = 60 * 24;
    private static final String REQUEST_KEY_PREFIX = "request:";
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public RentalRequest saveRequest(RentalRequest request) {
        String requestId = UtilFunction.hash(UUID.randomUUID().toString());
        request.setId(requestId);
        request.setExpiresAt(LocalDateTime.now().plusMinutes(REQUEST_TTL).toString());
        redisTemplate.opsForValue().set(
                REQUEST_KEY_PREFIX+requestId, request, REQUEST_TTL, TimeUnit.MINUTES);
        return request;
    }

    @Override
    public Optional<RentalRequest> getRequest(String requestId) {
        Object value = redisTemplate.opsForValue()
                .get(REQUEST_KEY_PREFIX + requestId);
        if (value == null) {
            return Optional.empty();
        }
        RentalRequest request = (RentalRequest) value;
        return Optional.of(request);
    }

    @Override
    public void removeRequest(String requestId) {
        redisTemplate.delete(REQUEST_KEY_PREFIX + requestId);
    }
}
