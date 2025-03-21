package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.LandlordFillContractRequest;
import com.c2se.roomily.payload.request.ModifyContractRequest;
import com.c2se.roomily.payload.request.TenantFillContractRequest;

public interface ContractService {
    void generateDefaultContract(String roomId);
    void modifyContract(ModifyContractRequest modifyContractRequest);
    void fillContractByLandlord(LandlordFillContractRequest landlordFillContractRequest);
    void fillContractByTenant(TenantFillContractRequest tenantFillContractRequest);
    String getDefaultContract(String roomId);
    String getContract(String rentedRoomId);
}
