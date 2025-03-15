package com.c2se.roomily.service.impl;

import com.c2se.roomily.payload.request.RentalRequest;
import com.c2se.roomily.service.RentalRequestCacheService;
import com.c2se.roomily.util.UtilFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RentalRequestCacheServiceImpl implements RentalRequestCacheService {
    private static final String REQUEST_KEY_PREFIX = "rental_request:";
    private static final int REQUEST_TTL = 60 * 60 * 24;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public RentalRequest saveRequest(RentalRequest request) {
        String requestId = UtilFunction.hash(UUID.randomUUID().toString());
        request.setId(requestId);
        request.setExpiresAt(LocalDateTime.now().plusMinutes(REQUEST_TTL));
        redisTemplate.opsForValue().set(
                REQUEST_KEY_PREFIX+requestId, request, REQUEST_TTL, TimeUnit.MINUTES);
        return request;
    }

    @Override
    public Optional<RentalRequest> getRequest(String requestId) {
        RentalRequest request = (RentalRequest) redisTemplate.opsForValue()
                .get(REQUEST_KEY_PREFIX + requestId);
        return Optional.of(request);
    }

    @Override
    public void removeRequest(String requestId) {
        redisTemplate.delete(REQUEST_KEY_PREFIX + requestId);
    }
}
