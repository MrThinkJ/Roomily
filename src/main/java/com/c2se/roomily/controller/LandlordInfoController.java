package com.c2se.roomily.controller;

import com.c2se.roomily.entity.LandlordInfo;
import com.c2se.roomily.payload.request.LandlordFillContractRequest;
import com.c2se.roomily.service.LandlordInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/landlord-info")
@RequiredArgsConstructor
public class LandlordInfoController extends BaseController {
    private final LandlordInfoService landlordInfoService;

    @GetMapping("/{landlordId}")
    @PreAuthorize("hasRole('ROLE_LANDLORD')")
    public ResponseEntity<LandlordInfo> getLandlordInfoByLandlordId(@PathVariable String landlordId) {
        return ResponseEntity.ok(landlordInfoService.getLandlordInfoByLandlordId(landlordId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_LANDLORD')")
    public ResponseEntity<LandlordInfo> saveLandlordInfo(@RequestBody LandlordFillContractRequest request) {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(landlordInfoService.saveLandlordInfo(userId, request));
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_LANDLORD')")
    public ResponseEntity<LandlordInfo> updateLandlordInfo(@RequestBody LandlordFillContractRequest request) {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(landlordInfoService.updateLandlordInfo(userId, request));
    }

    @GetMapping("/exists")
    @PreAuthorize("hasRole('ROLE_LANDLORD')")
    public ResponseEntity<Boolean> existsByUserId() {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(landlordInfoService.existsByUserId(userId));
    }
} 