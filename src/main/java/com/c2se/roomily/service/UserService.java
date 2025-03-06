package com.c2se.roomily.service;

import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.UserStatus;

import java.util.List;
import java.util.Set;

public interface UserService {
    User getUserEntity(String id);
    User getUserEntityByPrivateId(String privateId);
    Set<User> getUserEntities(List<String> ids);
    Set<User> getUserEntitiesByPrivateIds(List<String> privateIds);
    void updateUserStatus(User user, UserStatus status);

    void saveUser(User user);
}
