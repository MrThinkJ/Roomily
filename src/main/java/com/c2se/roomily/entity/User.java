package com.c2se.roomily.entity;

import com.c2se.roomily.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "phone")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private String id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String profilePicture;
    private String address;
    @Column(name = "rating")
    private Double rating = 0.00;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    @Column(name = "is_verified")
    private Boolean isVerified;
    @Column(name = "balance", precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.valueOf(0.00);
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;
}
