package com.c2se.roomily.service;

public interface TokenBlackListService {
    void addTokenToBlackList(String token);
    boolean isTokenBlackListed(String token);
}
