package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInFindPartnerPostResponse {
    private String userId;
    private String fullName;
    private String address;
    private String gender;
}
