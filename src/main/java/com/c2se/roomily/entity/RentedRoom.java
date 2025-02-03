package com.c2se.roomily.entity;

import com.c2se.roomily.enums.RentedRoomStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rented_rooms")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RentedRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rented_room_id")
    private String id;
    private LocalDateTime startDate;
    private Double duration;
    private RentedRoomStatus status;
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
