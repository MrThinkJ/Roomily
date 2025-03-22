package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class ModifyContractRequest {
    private String roomId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate contractDate;
    private String contractAddress;
    private String rentalAddress;
    private BigDecimal deposit;
    private List<String> responsibilitiesA;
    private List<String> responsibilitiesB;
    private List<String> commonResponsibilities;
}
