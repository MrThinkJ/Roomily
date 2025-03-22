package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.LandlordFillContractRequest;
import com.c2se.roomily.payload.request.ModifyContractRequest;
import com.c2se.roomily.payload.request.TenantFillContractRequest;
import com.c2se.roomily.payload.response.ContractResponsibilitiesResponse;
import com.c2se.roomily.payload.response.ContractUserInfoResponse;

import java.util.List;

public interface ContractService {
    void generateDefaultContract(String roomId);
    ContractResponsibilitiesResponse getContractResponsibilities(String userId, String roomId);
    void modifyContract(ModifyContractRequest modifyContractRequest);
    void fillContractByLandlord(LandlordFillContractRequest landlordFillContractRequest);
    void fillContractByTenant(TenantFillContractRequest tenantFillContractRequest);
    byte[] getDefaultContractPdfByRoomId(String roomId);
    byte[] getContractPdfByRentedRoomId(String rentedRoomId);
    String getDefaultContract(String roomId);
    ContractUserInfoResponse getContractUserInfo(String userId, String rentedRoomId);
    String getContract(String rentedRoomId);
}
