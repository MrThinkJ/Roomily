package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckBillLogRequest {
    private Boolean isElectricityChecked;
    private Boolean isWaterChecked;
    private String landlordComment;
}
