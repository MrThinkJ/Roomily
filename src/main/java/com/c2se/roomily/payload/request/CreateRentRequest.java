package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
public class CreateRentRequest {
    private String userId;
    private String roomId;
    private LocalDate startDate;
    private String findPartnerPostId;
    private String chatRoomId;
}
