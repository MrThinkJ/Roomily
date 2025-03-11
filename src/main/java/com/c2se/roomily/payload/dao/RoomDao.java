package com.c2se.roomily.payload.dao;

import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.Tag;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.RoomStatus;
import com.c2se.roomily.enums.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomDao {
    private String id;
    private String address;
    private String city;
    private String description;
    private String district;
    private BigDecimal electricPrice;
    private Integer maxPeople;
    private String nearbyAmenities;
    private BigDecimal price;
    private Double squareMeters;
    private String title;
    private String type;
    private String ward;
    private BigDecimal waterPrice;
    private Timestamp createdAt;
    private String deposit;
    private String status;
    private Timestamp updatedAt;
    private Double latitude;
    private Double longitude;
    private String landlordId;
    private boolean isSubscribed;
}
