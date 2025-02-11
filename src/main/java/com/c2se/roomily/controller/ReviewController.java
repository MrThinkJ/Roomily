package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.CreateReviewRequest;
import com.c2se.roomily.payload.response.ReviewResponse;
import com.c2se.roomily.service.ReviewService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController extends BaseController{
    ReviewService reviewService;
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable String id) {
        return ResponseEntity.ok(reviewService.getReview(id));
    }
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUserId(userId));
    }
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByRoomId(@PathVariable String roomId) {
        return ResponseEntity.ok(reviewService.getReviewsByRoomId(roomId));
    }
    @PostMapping("/rooms/{roomId}")
    public ResponseEntity<Boolean> createReview(@PathVariable String roomId,
                                                @RequestBody CreateReviewRequest createReviewRequest) {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(reviewService.createReview(userId, roomId, createReviewRequest));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateReview(@PathVariable String id,
                                                @RequestBody CreateReviewRequest createReviewRequest) {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(reviewService.updateReview(userId, id, createReviewRequest));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteReview(@PathVariable String id) {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(reviewService.deleteReview(userId, id));
    }
}
