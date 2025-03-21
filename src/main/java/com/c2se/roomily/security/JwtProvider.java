package com.c2se.roomily.security;

import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.exception.APIException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtProvider {
    @Value("${app.jwt.secret}")
    private String secretKey;
    @Value("${app.jwt.expiration}")
    private long expiration;

    public String generateToken(CustomUserDetails customUserDetails){
        String username = customUserDetails.getUsername();
        String[] roles = customUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toArray(String[]::new);
        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime()+expiration);
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setId(jti)
                .claim("roles", roles)
                .claim("userId", customUserDetails.getId())
                .setSubject(username)
                .setIssuedAt(currentDate)
                .setExpiration(expirationDate)
                .signWith(key())
                .compact();
    }

    public Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String getUsername(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Claims getClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parse(token);
            return true;
        } catch (MalformedJwtException malformedJwtException){
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_JWT);
        } catch (ExpiredJwtException expiredJwtException){
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.EXPIRED_JWT);
        } catch (UnsupportedJwtException unsupportedJwtException){
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.UNSUPPORTED_JWT);
        } catch (IllegalArgumentException e){
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_JWT_CLAIMS);
        }
    }
}
