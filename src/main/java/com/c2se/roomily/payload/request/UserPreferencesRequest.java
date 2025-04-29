package com.c2se.roomily.payload.request;

import com.c2se.roomily.enums.RoomType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class UserPreferencesRequest {
    private RoomType roomType;
    private String city;
    private String district;
    private String ward;
    private BigDecimal monthlySalary;
    private BigDecimal maxBudget;
    private List<String> mustHaveTagIds;
    private List<String> niceToHaveTagIds;
}
