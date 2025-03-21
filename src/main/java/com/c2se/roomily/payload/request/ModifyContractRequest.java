package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ModifyContractRequest {
    private String roomId;
    private String contractDay;
    private String contractMonth;
    private String contractYear;
    private String contractAddress;
    private String rentalAddress;
    private BigDecimal deposit;
    private List<String> responsibilitiesA;
    private List<String> responsibilitiesB;
    private List<String> responsibilitiesCommon;
}
