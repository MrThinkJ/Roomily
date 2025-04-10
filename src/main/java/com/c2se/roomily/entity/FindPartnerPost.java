package com.c2se.roomily.entity;

import com.c2se.roomily.enums.FindPartnerPostStatus;
import com.c2se.roomily.enums.FindPartnerPostType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "find_partner_posts")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindPartnerPost {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "find_partner_post_id")
    private String id;
    private String description;
    private Integer currentPeople;
    private Integer maxPeople;
    @Enumerated(EnumType.STRING)
    private FindPartnerPostStatus status;
    private String chatRoomId;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @ManyToOne
    @JoinColumn(name = "poster_id")
    private User poster;
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "find_partner_participants",
            joinColumns = @JoinColumn(name = "find_partner_post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();
    @Enumerated(EnumType.STRING)
    private FindPartnerPostType type;
    @ManyToOne
    @JoinColumn(name = "rented_room_id")
    private RentedRoom rentedRoom;
}
