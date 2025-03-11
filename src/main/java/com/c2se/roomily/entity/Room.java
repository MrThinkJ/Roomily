package com.c2se.roomily.entity;

import com.c2se.roomily.enums.RoomStatus;
import com.c2se.roomily.enums.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "rooms")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "room_id")
    private String id;
    @Column(name = "title")
    private String title;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    private String address;
    @Column(name = "room_status")
    @Enumerated(EnumType.STRING)
    private RoomStatus status;
    private BigDecimal price;
    private Double latitude;
    private Double longitude;
    private String city;
    private String district;
    private String ward;
    @Column(name = "electric_price")
    private BigDecimal electricPrice;
    @Column(name = "water_price")
    private BigDecimal waterPrice;
    @Column(name = "room_type")
    @Enumerated(EnumType.STRING)
    private RoomType type;
    @Column(name = "nearby_amenities")
    private String nearbyAmenities;
    private Integer maxPeople;
    private String rentalDeposit;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @ManyToOne
    @JoinColumn(name = "landlord_id")
    private User landlord;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "room_tags",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;
    @Column(name = "square_meters")
    private Double squareMeters;
}
