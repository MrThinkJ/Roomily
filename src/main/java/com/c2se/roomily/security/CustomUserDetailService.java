package com.c2se.roomily.security;

import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.enums.UserStatus;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.service.BanService;
import com.c2se.roomily.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserService userService;
    private final BanService banService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getUserEntityByUsernameOrEmail(username, username).orElseThrow(
                () -> new ResourceNotFoundException("User", "username", username)
        );
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.USER_DELETED, username);
        }
        if (banService.isUserBanned(user.getId())) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.USER_BANNED, username);
        }
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(role.getName()))
                .collect(java.util.stream.Collectors.toSet());

        return new CustomUserDetails(
                user.getUsername(),
                user.getPassword(),
                user.getId(),
                authorities
        );
    }
}
