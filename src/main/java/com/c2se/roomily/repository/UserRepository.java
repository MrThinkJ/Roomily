package com.c2se.roomily.repository;

import com.c2se.roomily.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
    Set<User> findByIdIn(List<String> ids);
}
