package com.c2se.roomily.security;

import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.exception.APIException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())){
            String token = getTokenFromStompHeaders(accessor);
            if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
                String username = jwtProvider.getUsername(token);
                Claims claims = jwtProvider.getClaims(token);
                String userId = claims.get("userId", String.class);
                List<String> roles = claims.get("roles", List.class);
                Set<GrantedAuthority> authorities = roles == null ? Set.of() : roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(java.util.stream.Collectors.toSet());
                CustomUserDetails userDetails = new CustomUserDetails(username, "", userId, authorities);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                accessor.setUser(authentication);
            }else {
                throw new APIException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_JWT);
            }
        }
        return message;
    }

    private String getTokenFromStompHeaders(StompHeaderAccessor stompHeaderAccessor) {
        String token = stompHeaderAccessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer")) {
            return token.substring(7);
        }
        return null;
    }
}
