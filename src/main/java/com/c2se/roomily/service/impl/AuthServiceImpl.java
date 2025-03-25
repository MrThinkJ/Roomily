package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.Role;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.enums.UserStatus;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.payload.request.LoginRequest;
import com.c2se.roomily.payload.request.RegisterRequest;
import com.c2se.roomily.payload.response.LoginResponse;
import com.c2se.roomily.payload.response.UserResponse;
import com.c2se.roomily.repository.RoleRepository;
import com.c2se.roomily.security.CustomUserDetails;
import com.c2se.roomily.security.JwtProvider;
import com.c2se.roomily.service.*;
import com.c2se.roomily.util.UtilFunction;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtProvider provider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final BanService banService;
    private final UserService userService;
    private final RoleService roleService;
    private final TokenBlackListService tokenBlackListService;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
            loginRequest.getUsernameOrEmail(),
            loginRequest.getPassword()
        ));
        Object principal = authentication.getPrincipal();
        if (principal == null){
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_CREDENTIALS);
        }
        
        CustomUserDetails customUserDetails = (CustomUserDetails) principal;
        String token = provider.generateToken(customUserDetails);
        String username = authentication.getName();
        User user = userService.getUserEntityByUsernameOrEmail(username, username).orElseThrow(
                () -> new UsernameNotFoundException("User not found with username or email: " + username)
        );

        if (!user.getStatus().equals(UserStatus.ACTIVE)){
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.USER_DELETED, username);
        }
        if (banService.isUserBanned(user.getId())){
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.USER_BANNED, username);
        }

        return LoginResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }

    @Override
    public void logout(String token) {
        tokenBlackListService.addTokenToBlackList(token);
    }

    @Override
    public void register(RegisterRequest registerRequest) {
        User user = userService.getUserEntityByUsernameOrEmail(registerRequest.getUsername(), registerRequest.getEmail())
                .orElse(null);
        if (user != null){
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.EXISTING_USERNAME_OR_EMAIL);
        }
        Role role = registerRequest.isLandlord() ?
                roleService.getByName("ROLE_LANDLORD") : roleService.getByName("ROLE_USER");
        Set<Role> roles = Set.of(role);
        user = User.builder()
                .username(registerRequest.getUsername())
                .privateId(UtilFunction.hash(registerRequest.getUsername()))
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .gender(registerRequest.getGender())
                .phone(registerRequest.getPhone())
                .profilePicture(null)
                .address(registerRequest.getAddress())
                .status(UserStatus.ACTIVE)
                .isVerified(false)
                .balance(BigDecimal.ZERO)
                .rating(0.00)
                .roles(roles)
                .build();
        userService.saveUser(user);
    }

    @Override
    public UserResponse me(String userId) {
        User user = userService.getUserEntity(userId);
        return UserResponse.builder()
                .id(userId)
                .phone(user.getPhone())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .gender(user.getGender())
                .profilePicture(user.getProfilePicture())
                .address(user.getAddress())
                .rating(user.getRating())
                .isVerified(user.getIsVerified())
                .balance(user.getBalance())
                .privateId(user.getPrivateId())
                .build();
    }
}
