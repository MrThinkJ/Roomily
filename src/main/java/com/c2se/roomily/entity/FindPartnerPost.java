package com.c2se.roomily.entity;

import com.c2se.roomily.enums.FindPartnerPostStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String content;
    private String title;
    private Integer currentPeople;
    private Integer maxPeople;
    @Column(name = "chat_room_id")
    private String chatRoomId;
    @Enumerated(EnumType.STRING)
    private FindPartnerPostStatus status;
    @ManyToOne
    @JoinColumn(name = "poster_id")
    private User poster;
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "find_partner_participants",
            joinColumns = @JoinColumn(name = "find_partner_post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants;
}
