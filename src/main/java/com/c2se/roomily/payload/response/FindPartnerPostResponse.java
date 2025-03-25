package com.c2se.roomily.payload.response;

import com.c2se.roomily.enums.FindPartnerPostStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FindPartnerPostResponse {
    private String findPartnerPostId;
    private Integer currentPeople;
    private Integer maxPeople;
    private String status;
    private String posterId;
    private String roomId;
    private String rentedRoomId;
    private List<UserInFindPartnerPostResponse> participants;
    private String type;
}
