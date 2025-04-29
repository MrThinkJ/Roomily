package com.c2se.roomily.payload.response;

import com.c2se.roomily.enums.FindPartnerPostStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class FindPartnerPostResponse {
    private String findPartnerPostId;
    private Integer currentPeople;
    private String description;
    private Integer maxPeople;
    private String status;
    private String posterId;
    private String roomId;
    private String rentedRoomId;
    private List<UserInFindPartnerPostResponse> participants;
    private String type;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
