package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.CreateLandlordReviewRequest;
import com.c2se.roomily.payload.response.LandlordReviewResponse;
import com.c2se.roomily.service.LandlordReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/landlord-reviews")
public class LandlordReviewController extends BaseController{
    LandlordReviewService landlordReviewService;
    @GetMapping("/{id}")
    public ResponseEntity<LandlordReviewResponse> getLandlordReview(@PathVariable String id) {
        return ResponseEntity.ok(landlordReviewService.getLandlordReview(id));
    }
    @GetMapping("/landlord/{landlordId}")
    public ResponseEntity<List<LandlordReviewResponse>> getLandlordReviewsByLandlordId(
            @PathVariable String landlordId) {
        return ResponseEntity.ok(landlordReviewService.getLandlordReviewsByLandlordId(landlordId));
    }
    @GetMapping("/reviewer/{reviewerId}")
    public ResponseEntity<List<LandlordReviewResponse>> getLandlordReviewsByReviewerId(
            @PathVariable String reviewerId) {
        return ResponseEntity.ok(landlordReviewService.getLandlordReviewsByReviewerId(reviewerId));
    }
    @GetMapping("/landlord/{landlordId}/reviewer/{reviewerId}")
    public ResponseEntity<List<LandlordReviewResponse>> getLandlordReviewsByLandlordIdAndReviewerId(
            @PathVariable String landlordId,
            @PathVariable String reviewerId) {
        return ResponseEntity.ok(landlordReviewService.getLandlordReviewsByLandlordIdAndReviewerId(landlordId, reviewerId));
    }
    @PostMapping("/landlord/{landlordId}")
    public ResponseEntity<Boolean> createLandlordReview(
            @PathVariable String landlordId,
            @RequestBody CreateLandlordReviewRequest createLandlordReviewRequest) {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(landlordReviewService.createLandlordReview(
                userId, landlordId, createLandlordReviewRequest));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateLandlordReview(
            @PathVariable String id,
            @RequestBody CreateLandlordReviewRequest createLandlordReviewRequest) {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(landlordReviewService.updateLandlordReview(
                id, userId, createLandlordReviewRequest));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteLandlordReview(@PathVariable String id) {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(landlordReviewService.deleteLandlordReview(id, userId));
    }
}
