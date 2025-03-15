package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.UserStatus;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User getUserEntity(String id) {
        return userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", id)
        );
    }

    @Override
    public User getCurrentUser() {
        return null;
    }

    @Override
    public Optional<User> getUserEntityByUsernameOrEmail(String username, String email) {
        return userRepository.findByUsernameOrEmail(username, email);
    }

    @Override
    public User getUserEntityByPrivateId(String privateId) {
        return userRepository.findByPrivateId(privateId).orElseThrow(
                () -> new ResourceNotFoundException("User", "privateId", privateId)
        );
    }

    @Override
    public Set<User> getUserEntities(List<String> ids) {
        return userRepository.findByIdIn(ids);
    }

    @Override
    public Set<User> getUserEntitiesByPrivateIds(List<String> privateIds) {
        return userRepository.findByPrivateIdIn(privateIds);
    }

    @Override
    public void updateUserStatus(User user, UserStatus status) {
        user.setStatus(status);
        userRepository.save(user);
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }
}
