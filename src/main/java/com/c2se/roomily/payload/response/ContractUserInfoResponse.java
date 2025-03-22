package com.c2se.roomily.payload.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ContractUserInfoResponse {
    private String fullName;
    private LocalDate dateOfBirth;
    private String permanentResidence;
    private String identityNumber;
    private LocalDate identityProvidedDate;
    private String identityProvidedPlace;
    private String phoneNumber;
}