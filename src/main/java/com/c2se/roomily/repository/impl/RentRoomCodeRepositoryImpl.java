package com.c2se.roomily.repository.impl;

import com.c2se.roomily.repository.RentRoomCodeRepository;
import org.springframework.stereotype.Repository;

@Repository
public class RentRoomCodeRepositoryImpl implements RentRoomCodeRepository {

    @Override
    public String save(String userId, String rentRoomCode) {
        return null;
    }

    @Override
    public String findByCode(String rentRoomCode) {
        return null;
    }
}
