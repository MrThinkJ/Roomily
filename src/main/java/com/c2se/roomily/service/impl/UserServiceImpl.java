package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.UserStatus;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.UpdateUserRequest;
import com.c2se.roomily.payload.response.PageUserResponse;
import com.c2se.roomily.payload.response.UserResponse;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        return userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
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
    public UserResponse getUserByUserId(String id) {
        return mapToUserResponse(getUserEntity(id));
    }

    @Override
    public UserResponse getUserByPrivateId(String privateId) {
        return mapToUserResponse(getUserEntityByPrivateId(privateId));
    }

    @Override
    public PageUserResponse getUsers(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.fromString(sortDir), sortBy);
        Page<User> users = userRepository.findAll(pageable);
        return mapToPageUserResponse(users);
    }

    @Override
    public PageUserResponse getUsersByStatus(String userStatus, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.fromString(sortDir), sortBy);
        Page<User> users = userRepository.findByStatus(UserStatus.valueOf(userStatus), pageable);
        return mapToPageUserResponse(users);
    }

    @Override
    public PageUserResponse getUsersByIsVerified(Boolean isVerified, int page, int size, String sortBy,
                                                 String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.fromString(sortDir), sortBy);
        Page<User> users = userRepository.findByIsVerified(isVerified, pageable);
        return mapToPageUserResponse(users);
    }

    @Override
    public PageUserResponse getUsersByRatingInRange(Double minRating, Double maxRating, int page, int size,
                                                    String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.fromString(sortDir), sortBy);
        Page<User> users = userRepository.findByRatingBetween(minRating, maxRating, pageable);
        return mapToPageUserResponse(users);
    }

    @Override
    public void updateUser(String userId, UpdateUserRequest request) {
        User user = getUserEntity(userId);
        user.setFullName(request.getFullName() != null ? request.getFullName() : user.getFullName());
        user.setEmail(request.getEmail() != null ? request.getEmail() : user.getEmail());
        user.setPhone(request.getPhone() != null ? request.getPhone() : user.getPhone());
        user.setAddress(request.getAddress() != null ? request.getAddress() : user.getAddress());
        userRepository.save(user);
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

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .balance(user.getBalance())
                .rating(user.getRating())
                .privateId(user.getPrivateId())
                .isVerified(user.getIsVerified())
                .address(user.getAddress())
                .profilePicture(user.getProfilePicture())
                .build();
    }

    private PageUserResponse mapToPageUserResponse(Page<User> users) {
        List<UserResponse> userResponses = users.getContent().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
        return PageUserResponse.builder()
                .users(userResponses)
                .totalPages(users.getTotalPages())
                .totalElements(users.getTotalElements())
                .currentPage(users.getNumber())
                .pageSize(users.getSize())
                .hasNext(users.hasNext())
                .hasPrevious(users.hasPrevious())
                .isFirst(users.isFirst())
                .isLast(users.isLast())
                .build();
    }
}
