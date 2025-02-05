package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.LandlordReview;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.CreateLandlordReviewRequest;
import com.c2se.roomily.payload.response.LandlordReviewResponse;
import com.c2se.roomily.payload.response.ReviewResponse;
import com.c2se.roomily.repository.LandlordReviewRepository;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.service.LandlordReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LandlordReviewServiceImpl implements LandlordReviewService {
    UserRepository userRepository;
    LandlordReviewRepository landlordReviewRepository;

    @Override
    public LandlordReviewResponse getLandlordReview(String reviewId) {
        LandlordReview review = landlordReviewRepository.findById(reviewId).orElseThrow(
                () -> new ResourceNotFoundException("LandlordReview", "id", reviewId)
        );
        return mapReviewToReviewResponse(review);
    }

    @Override
    public List<LandlordReviewResponse> getLandlordReviewsByLandlordId(String landlordId) {
        List<LandlordReview> reviews = landlordReviewRepository.findByLandlordId(landlordId);
        return reviews.stream()
                .map(this::mapReviewToReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LandlordReviewResponse> getLandlordReviewsByReviewerId(String reviewerId) {
        List<LandlordReview> reviews = landlordReviewRepository.findByReviewerId(reviewerId);
        return reviews.stream()
                .map(this::mapReviewToReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LandlordReviewResponse> getLandlordReviewsByLandlordIdAndReviewerId(String landlordId,
                                                                                    String reviewerId) {
        List<LandlordReview> reviews = landlordReviewRepository.findByLandlordIdAndReviewerId(landlordId, reviewerId);
        return reviews.stream()
                .map(this::mapReviewToReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean createLandlordReview(String reviewerId, String landlordId,
                                        CreateLandlordReviewRequest createLandlordReviewRequest) {
        User reviewer = userRepository.findById(reviewerId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", reviewerId)
        );
        User landlord = userRepository.findById(landlordId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", landlordId)
        );
        LandlordReview review = LandlordReview.builder()
                .reviewer(reviewer)
                .landlord(landlord)
                .rating(createLandlordReviewRequest.getRating())
                .content(createLandlordReviewRequest.getContent())
                .build();
        landlordReviewRepository.save(review);
        return true;
    }

    @Override
    public Boolean updateLandlordReview(String reviewId, String reviewerId,
                                        CreateLandlordReviewRequest createLandlordReviewRequest) {
        LandlordReview review = landlordReviewRepository.findById(reviewId).orElseThrow(
                ()-> new ResourceNotFoundException("LandlordReview", "id", reviewId)
        );
        if (!review.getReviewer().getId().equals(reviewerId)) {
            throw new ResourceNotFoundException("LandlordReview", "id", reviewId);
        }
        review.setContent(createLandlordReviewRequest.getContent());
        review.setRating(createLandlordReviewRequest.getRating());
        landlordReviewRepository.save(review);
        return true;
    }

    @Override
    public Boolean deleteLandlordReview(String reviewId, String reviewerId) {
        LandlordReview review = landlordReviewRepository.findById(reviewId).orElseThrow(
                () -> new ResourceNotFoundException("LandlordReview", "id", reviewId)
        );
        if (!review.getReviewer().getId().equals(reviewerId)) {
            throw new ResourceNotFoundException("LandlordReview", "id", reviewId);
        }
        landlordReviewRepository.delete(review);
        return true;
    }

    private LandlordReviewResponse mapReviewToReviewResponse(LandlordReview review) {
        return LandlordReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .landlordId(review.getLandlord().getId())
                .reviewerId(review.getReviewer().getId())
                .build();
    }
}
