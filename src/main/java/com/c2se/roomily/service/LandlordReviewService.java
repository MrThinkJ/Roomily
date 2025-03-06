package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.CreateLandlordReviewRequest;
import com.c2se.roomily.payload.response.LandlordReviewResponse;

import java.util.List;

public interface LandlordReviewService {
    LandlordReviewResponse getLandlordReview(String reviewId);

    List<LandlordReviewResponse> getLandlordReviewsByLandlordId(String landlordId);

    List<LandlordReviewResponse> getLandlordReviewsByReviewerId(String reviewerId);

    Boolean createLandlordReview(String reviewerId, String landlordId,
                                 CreateLandlordReviewRequest createLandlordReviewRequest);

    Boolean updateLandlordReview(String reviewId, String reviewerId,
                                 CreateLandlordReviewRequest createLandlordReviewRequest);

    Boolean deleteLandlordReview(String reviewId, String reviewerId);

    List<LandlordReviewResponse> getLandlordReviewsByLandlordIdAndReviewerId(String landlordId, String reviewerId);
}
