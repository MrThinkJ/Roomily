package com.c2se.roomily.payload.response;

import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.enums.RentedRoomActivityType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
public class RentedRoomActivityResponse {
    private String id;
    private String rentedRoomId;
    private String activityType;
    private String message;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
