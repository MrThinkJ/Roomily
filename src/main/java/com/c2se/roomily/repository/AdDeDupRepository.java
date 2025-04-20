package com.c2se.roomily.repository;

import com.c2se.roomily.payload.request.AdClickRequest;

public interface AdDeDupRepository {
    boolean save(AdClickRequest adClickRequest);
    void deleteById(String id);
}
