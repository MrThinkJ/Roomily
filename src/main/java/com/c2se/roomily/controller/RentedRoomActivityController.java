package com.c2se.roomily.controller;

import com.c2se.roomily.payload.response.RentedRoomActivityResponse;
import com.c2se.roomily.service.RentedRoomActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rented-room-activities")
@RequiredArgsConstructor
public class RentedRoomActivityController {
    private final RentedRoomActivityService rentedRoomActivityService;

    @GetMapping("/{rentedRoomId}")
    public ResponseEntity<List<RentedRoomActivityResponse>> getRentedRoomActivitiesByRentedRoomId(
            @PathVariable String rentedRoomId,
            @RequestParam String pivotId,
            @RequestParam String timestamp,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(rentedRoomActivityService.getRentedRoomActivitiesByRentedRoomId(rentedRoomId, pivotId,
                                                                                                 timestamp, limit));
    }
}
