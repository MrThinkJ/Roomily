package com.c2se.roomily.repository;

public interface FindPartnerRequestRepository extends RedisRepository<String, String>{
    String generateKey(String userId, String findPartnerPostId, int ttl);
}
