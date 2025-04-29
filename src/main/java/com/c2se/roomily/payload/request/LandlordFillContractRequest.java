package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
public class LandlordFillContractRequest {
    private String landlordFullName;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate landlordDateOfBirth;
    private String landlordPermanentResidence;
    private String landlordIdentityNumber;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate landlordIdentityProvidedDate;
    private String landlordIdentityProvidedPlace;
    private String landlordPhoneNumber;
}
