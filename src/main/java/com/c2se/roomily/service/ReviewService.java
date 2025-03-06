package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.CreateReviewRequest;
import com.c2se.roomily.payload.response.ReviewResponse;

import java.util.List;

public interface ReviewService {
    Boolean createReview(String userId, String roomId, CreateReviewRequest createReviewRequest);

    ReviewResponse getReview(String reviewId);

    List<ReviewResponse> getReviewsByUserId(String userId);

    List<ReviewResponse> getReviewsByRoomId(String roomId);

    Boolean updateReview(String userId, String reviewId, CreateReviewRequest createReviewRequest);

    Boolean deleteReview(String userId, String reviewId);
}
