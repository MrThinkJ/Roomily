package com.c2se.roomily.service;

import com.c2se.roomily.entity.LandlordInfo;
import com.c2se.roomily.payload.request.LandlordFillContractRequest;
import com.c2se.roomily.payload.response.ContractUserInfoResponse;

public interface LandlordInfoService {
    LandlordInfo getLandlordInfoByLandlordId(String landlordId);
    LandlordInfo saveLandlordInfo(String userId, LandlordFillContractRequest request);
    LandlordInfo updateLandlordInfo(String userId, LandlordFillContractRequest request);
    boolean existsByUserId(String userId);
} 