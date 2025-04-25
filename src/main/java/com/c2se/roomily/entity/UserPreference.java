package com.c2se.roomily.entity;

import com.c2se.roomily.enums.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user_preferences")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_preference_id")
    private String id;
    private String city;
    private String district;
    private String ward;
    @Enumerated(EnumType.STRING)
    private RoomType roomType;
    @Column(name = "max_budget", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxBudget;
    @Column(name = "monthly_salary", precision = 10, scale = 2)
    private BigDecimal monthlySalary;
    private String userId;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_preference_must_tags",
            joinColumns = @JoinColumn(name = "user_preference_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> mustHaveTags = new HashSet<>();
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_preference_nice_tags",
            joinColumns = @JoinColumn(name = "user_preference_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> niceToHaveTags = new HashSet<>();
}
