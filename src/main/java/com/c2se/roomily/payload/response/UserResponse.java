package com.c2se.roomily.payload.response;

import com.c2se.roomily.entity.Role;
import com.c2se.roomily.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
public class UserResponse {
    private String id;
    private String privateId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String profilePicture;
    private String address;
    private Double rating;
    private Boolean isVerified;
    private BigDecimal balance;
}
