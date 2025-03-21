package com.c2se.roomily.security;

import com.c2se.roomily.service.TokenBlackListService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final TokenBlackListService tokenBlackListService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
            if (tokenBlackListService.isTokenBlackListed(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is revoked");
                return;
            }
            String username = jwtProvider.getUsername(token);
            Claims claims = jwtProvider.getClaims(token);
            String userId = claims.get("userId", String.class);
            List roles = claims.get("roles", List.class);
            Set<GrantedAuthority> authSet = new HashSet<>();
            if (roles != null) {
                for (Object role : roles) {
                    if (!(role instanceof String)) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid token");
                        return;
                    }
                    authSet.add(new SimpleGrantedAuthority((String) role));
                }
            }
            CustomUserDetails userDetails = new CustomUserDetails(username, "", userId, authSet);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer")) {
            return token.substring(7);
        }
        return null;
    }
}
