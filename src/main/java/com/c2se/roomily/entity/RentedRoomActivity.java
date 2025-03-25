package com.c2se.roomily.entity;

import com.c2se.roomily.enums.RentedRoomActivityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rented_room_activities")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RentedRoomActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rented_room_activity_id")
    private String id;
    @ManyToOne
    @JoinColumn(name = "rented_room_id")
    private RentedRoom rentedRoom;
    @Column(name = "message", length = 500)
    private String message;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
