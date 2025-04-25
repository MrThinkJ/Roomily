package com.c2se.roomily.repository;

import com.c2se.roomily.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, String> {
    Optional<UserPreference> findByUserId(String userId);
    boolean existsByUserId(String userId);
}
