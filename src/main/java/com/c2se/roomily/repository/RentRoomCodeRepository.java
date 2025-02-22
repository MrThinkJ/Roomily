package com.c2se.roomily.repository;

public interface RentRoomCodeRepository {
    String save(String userId, String rentRoomCode);
    String findByUserId(String userId);
}
