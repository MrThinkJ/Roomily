package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
public class TenantFillContractRequest {
    private String rentedRoomId;
    private String tenantFullName;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate tenantDateOfBirth;
    private String tenantPermanentResidence;
    private String tenantIdentityNumber;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate tenantIdentityProvidedDate;
    private String tenantIdentityProvidedPlace;
    private String tenantPhoneNumber;
}
