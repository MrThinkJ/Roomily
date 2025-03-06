package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.UserStatus;
import com.c2se.roomily.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    @Override
    public User getUserEntity(String id) {
        return null;
    }

    @Override
    public User getUserEntityByPrivateId(String privateId) {
        return null;
    }

    @Override
    public Set<User> getUserEntities(List<String> ids) {
        return null;
    }

    @Override
    public Set<User> getUserEntitiesByPrivateIds(List<String> privateIds) {
        return null;
    }

    @Override
    public void updateUserStatus(User user, UserStatus status) {

    }

    @Override
    public void saveUser(User user) {

    }
}
