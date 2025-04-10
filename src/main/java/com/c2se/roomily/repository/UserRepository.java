package com.c2se.roomily.repository;

import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
    @Query("SELECT u FROM User u WHERE 'ROLE_LANDLORD' IN (SELECT r.name FROM u.roles r)")
    List<User> findAllLandlords();
    @Query("SELECT u FROM User u WHERE 'ROLE_TENANT' IN (SELECT r.name FROM u.roles r)")
    List<User> findAllTenants();
    Optional<User> findByUsernameOrEmail(String username, String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByPrivateId(String privateId);
    Set<User> findByIdIn(List<String> ids);
    Set<User> findByPrivateIdIn(List<String> privateIds);
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    Page<User> findByIsVerified(Boolean isVerified, Pageable pageable);
    Page<User> findByRatingBetween(Double minRating, Double maxRating, Pageable pageable);
}
