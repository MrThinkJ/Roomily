package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.Review;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.CreateReviewRequest;
import com.c2se.roomily.payload.response.ReviewResponse;
import com.c2se.roomily.repository.ReviewRepository;
import com.c2se.roomily.repository.RoomRepository;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    UserRepository userRepository;
    ReviewRepository reviewRepository;
    RoomRepository roomRepository;
    @Override
    public Boolean createReview(String userId, String roomId, CreateReviewRequest createReviewRequest) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", userId)
        );
        Room room = roomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("Room", "id", roomId)
        );
        Review review = Review.builder()
                .user(user)
                .room(room)
                .rating(createReviewRequest.getRating())
                .content(createReviewRequest.getContent())
                .build();
        reviewRepository.save(review);
        return true;
    }

    @Override
    public ReviewResponse getReview(String reviewId) {
        return reviewRepository.findById(reviewId)
                .map(this::mapReviewToReviewResponse)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Review", "id", reviewId)
                );
    }

    @Override
    public List<ReviewResponse> getReviewsByUserId(String userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviews.stream()
                .map(this::mapReviewToReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getReviewsByRoomId(String roomId) {
        return reviewRepository.findByRoomId(roomId).stream()
                .map(this::mapReviewToReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean updateReview(String userId, String reviewId, CreateReviewRequest createReviewRequest) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(
                () -> new ResourceNotFoundException("Review", "id", reviewId)
        );
        if (!review.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Review", "id", reviewId);
        }
        review.setContent(createReviewRequest.getContent());
        review.setRating(createReviewRequest.getRating());
        reviewRepository.save(review);
        return true;
    }

    @Override
    public Boolean deleteReview(String userId, String reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(
                () -> new ResourceNotFoundException("Review", "id", reviewId)
        );
        if (!review.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Review", "id", reviewId);
        }
        reviewRepository.delete(review);
        return true;
    }

    private ReviewResponse mapReviewToReviewResponse(Review review) {
        User user = review.getUser();
        return ReviewResponse.builder()
                .id(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .userId(review.getUser().getId())
                .roomId(review.getRoom().getId())
                .userName(user.getUsername())
                .userAvatar(user.getAvatar())
                .build();
    }
}
