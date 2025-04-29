package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.LandlordInfo;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.payload.request.LandlordFillContractRequest;
import com.c2se.roomily.repository.LandlordInfoRepository;
import com.c2se.roomily.service.LandlordInfoService;
import com.c2se.roomily.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LandlordInfoServiceImpl implements LandlordInfoService {
    private final LandlordInfoRepository landlordInfoRepository;
    private final UserService userService;

    @Override
    public LandlordInfo getLandlordInfoByLandlordId(String landlordId) {
        return landlordInfoRepository.findByUserId(landlordId).orElse(null);
    }

    @Override
    public LandlordInfo saveLandlordInfo(String userId, LandlordFillContractRequest request) {
        User user = userService.getUserEntityById(userId);
        
        LandlordInfo landlordInfo = landlordInfoRepository.findByUserId(userId)
                .orElse(LandlordInfo.builder()
                        .user(user)
                        .build());
        
        landlordInfo.setFullName(request.getLandlordFullName());
        landlordInfo.setDateOfBirth(request.getLandlordDateOfBirth());
        landlordInfo.setPermanentResidence(request.getLandlordPermanentResidence());
        landlordInfo.setIdentityNumber(request.getLandlordIdentityNumber());
        landlordInfo.setIdentityProvidedDate(request.getLandlordIdentityProvidedDate());
        landlordInfo.setIdentityProvidedPlace(request.getLandlordIdentityProvidedPlace());
        landlordInfo.setPhoneNumber(request.getLandlordPhoneNumber());
        return landlordInfoRepository.save(landlordInfo);
    }

    @Override
    public LandlordInfo updateLandlordInfo(String userId, LandlordFillContractRequest request) {
        LandlordInfo landlordInfo = getLandlordInfoByLandlordId(userId);
        if (request.getLandlordFullName() != null) {  
            landlordInfo.setFullName(request.getLandlordFullName());
        }
        if (request.getLandlordDateOfBirth() != null) {
            landlordInfo.setDateOfBirth(request.getLandlordDateOfBirth());
        }
        if (request.getLandlordPermanentResidence() != null) {
            landlordInfo.setPermanentResidence(request.getLandlordPermanentResidence());
        }
        if (request.getLandlordIdentityNumber() != null) {
            landlordInfo.setIdentityNumber(request.getLandlordIdentityNumber());
        }
        if (request.getLandlordIdentityProvidedDate() != null) {
            landlordInfo.setIdentityProvidedDate(request.getLandlordIdentityProvidedDate());
        }
        if (request.getLandlordIdentityProvidedPlace() != null) {
            landlordInfo.setIdentityProvidedPlace(request.getLandlordIdentityProvidedPlace());
        }
        if (request.getLandlordPhoneNumber() != null) {
            landlordInfo.setPhoneNumber(request.getLandlordPhoneNumber());
        }
        return landlordInfoRepository.save(landlordInfo);
    }

    @Override
    public boolean existsByUserId(String userId) {
        return landlordInfoRepository.existsByUserId(userId);
    }
}