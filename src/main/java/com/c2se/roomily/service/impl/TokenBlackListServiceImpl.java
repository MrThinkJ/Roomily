package com.c2se.roomily.service.impl;

import com.c2se.roomily.security.JwtProvider;
import com.c2se.roomily.service.TokenBlackListService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlackListServiceImpl implements TokenBlackListService {
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProvider jwtProvider;
    private static final String BLACKLIST_PREFIX = "BLACKLIST_";

    @Override
    public void addTokenToBlackList(String token) {
        Claims claims = jwtProvider.getClaims(token);
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX+claims.getId(),
                                        "1",
                                        (claims.getExpiration().getTime() - System.currentTimeMillis()),
                                        TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isTokenBlackListed(String token) {
        Claims claims = jwtProvider.getClaims(token);
        return redisTemplate.hasKey(BLACKLIST_PREFIX+claims.getId());
    }
}
