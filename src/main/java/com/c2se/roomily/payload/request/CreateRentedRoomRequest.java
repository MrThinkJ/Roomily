package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CreateRentedRoomRequest {
    private String roomId;
    private LocalDate startDate;
    private String findPartnerPostId;
}
