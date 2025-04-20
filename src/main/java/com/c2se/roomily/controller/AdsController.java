package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.*;
import com.c2se.roomily.payload.response.AdCampaignResponse;
import com.c2se.roomily.payload.response.AdClickResponse;
import com.c2se.roomily.payload.response.PromotedRoomResponse;
import com.c2se.roomily.service.AdsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ads/")
@RequiredArgsConstructor
public class AdsController extends BaseController{
    private final AdsService adsService;

    @GetMapping("/campaigns/{campaignId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<AdCampaignResponse> getCampaign(@PathVariable String campaignId) {
        String userId = this.getUserInfo().getId();
        AdCampaignResponse response = adsService.getCampaign(userId, campaignId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/campaigns")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<List<AdCampaignResponse>> getUserCampaigns() {
        String userId = this.getUserInfo().getId();
        List<AdCampaignResponse> response = adsService.getUserCampaigns(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/campaigns")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<Void> createCampaign(@RequestBody CreateCampaignRequest request) {
        String userId = this.getUserInfo().getId();
        adsService.createCampaign(userId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/campaigns/{campaignId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<Void> updateCampaign(@PathVariable String campaignId,
                                                             @RequestBody UpdateCampaignRequest request) {
        String userId = this.getUserInfo().getId();
        adsService.updateCampaign(userId, campaignId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/campaigns/{campaignId}/pause")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<Void> pauseCampaign(@PathVariable String campaignId) {
        String userId = this.getUserInfo().getId();
        adsService.pauseCampaign(userId, campaignId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/campaigns/{campaignId}/resume")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<Void> resumeCampaign(@PathVariable String campaignId) {
        String userId = this.getUserInfo().getId();
        adsService.resumeCampaign(userId, campaignId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/campaigns/{campaignId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<Void> deleteCampaign(@PathVariable String campaignId) {
        String userId = this.getUserInfo().getId();
        adsService.deleteCampaign(userId, campaignId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/promoted-rooms/campaigns/{campaignId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<List<PromotedRoomResponse>> getPromotedRoomsByCampaign(@PathVariable String campaignId) {
        String userId = this.getUserInfo().getId();
        List<PromotedRoomResponse> response = adsService.getPromotedRoomsByCampaign(userId, campaignId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/promoted-rooms/campaigns/{campaignId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<Void> addRoomToCampaign(@PathVariable String campaignId,
                                                  @RequestBody AddRoomRequest request) {
        String userId = this.getUserInfo().getId();
        adsService.addRoomToCampaign(userId, campaignId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/promoted-rooms/{promotedRoomId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<Void> updatePromotedRoom(@PathVariable String promotedRoomId,
                                                   @RequestBody UpdatePromotedRoomRequest request) {
        String userId = this.getUserInfo().getId();
        adsService.updatePromotedRoom(userId, promotedRoomId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/promoted-rooms/{promotedRoomId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<Void> removeRoomFromCampaign(@PathVariable String promotedRoomId) {
        String userId = this.getUserInfo().getId();
        adsService.removeRoomFromCampaign(userId, promotedRoomId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/promoted-rooms/click")
    public ResponseEntity<AdClickResponse> recordClick(@RequestBody AdClickRequest request) {
        return ResponseEntity.ok(adsService.recordClick(request));
    }

    @PatchMapping("/impression")
    public ResponseEntity<Void> recordImpression(@RequestBody AdImpressionRequest request) {
        adsService.recordImpression(request);
        return ResponseEntity.ok().build();
    }
}
