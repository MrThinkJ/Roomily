package com.c2se.roomily.service;

import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.UserStatus;
import com.c2se.roomily.payload.request.UpdateUserRequest;
import com.c2se.roomily.payload.response.PageUserResponse;
import com.c2se.roomily.payload.response.UserResponse;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserService {
    User getUserEntityById(String id);

    boolean isUserExists(String id);

    User getCurrentUser();

    Optional<User> getUserEntityByUsernameOrEmail(String username, String email);

    User getUserEntityByPrivateId(String privateId);

    Set<User> getUserEntities(List<String> ids);

    Set<User> getUserEntitiesByPrivateIds(List<String> privateIds);

    UserResponse getUserByUserId(String id);

    UserResponse getUserByPrivateId(String privateId);

    PageUserResponse getUsers(int page, int size, String sortBy, String sortDir);

    PageUserResponse getUsersByStatus(String userStatus, int page, int size, String sortBy, String sortDir);

    PageUserResponse getUsersByIsVerified(Boolean isVerified, int page, int size, String sortBy, String sortDir);

    PageUserResponse getUsersByRatingInRange(Double minRating, Double maxRating, int page, int size, String sortBy,
            String sortDir);

    void updateUser(String userId, UpdateUserRequest request);

    void updateUserStatus(User user, UserStatus status);

    void saveUser(User user);
}
