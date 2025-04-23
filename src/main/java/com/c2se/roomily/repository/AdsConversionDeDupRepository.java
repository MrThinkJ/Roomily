package com.c2se.roomily.repository;

public interface AdsConversionDeDupRepository {
    boolean save(String userId, String chatRoomId);
    void deleteById(String id);
}
