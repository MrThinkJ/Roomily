package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ContractResponsibilitiesResponse {
    private List<String> responsibilitiesA;
    private List<String> responsibilitiesB;
    private List<String> commonResponsibilities;

}
