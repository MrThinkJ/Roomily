package com.c2se.roomily.repository;

public interface RedisRepository<K, V> {
    void save(K key, V value, int ttl);

    String findByKey(K key);

    void deleteByKey(K key);
}
