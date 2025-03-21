package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.CreateRentedRoomRequest;
import com.c2se.roomily.payload.request.RentalRequest;
import com.c2se.roomily.payload.response.RentedRoomResponse;
import com.c2se.roomily.service.RequestCacheService;
import com.c2se.roomily.service.RentedRoomService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rented-rooms")
@AllArgsConstructor
public class RentedRoomController extends BaseController {
    RentedRoomService rentedRoomService;
    RequestCacheService rentalRequestCacheService;

    @GetMapping
    public ResponseEntity<List<RentedRoomResponse>> getRentedRoomsByUserId() {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(rentedRoomService.getRentedRoomActiveByUserIdOrCoTenantId(userId));
    }

    @GetMapping("/landlord/{landlordId}")
    public ResponseEntity<List<RentedRoomResponse>> getRentedRoomsByLandlordId(@PathVariable String landlordId) {
        return ResponseEntity.ok(rentedRoomService.getRentedRoomsByLandlordId(landlordId));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<RentedRoomResponse> getRentedRoomByRoomId(@PathVariable String roomId) {
        return ResponseEntity.ok(rentedRoomService.getActiveRentedRoomByRoomId(roomId));
    }

    @GetMapping("/history/{roomId}")
    public ResponseEntity<List<RentedRoomResponse>> getRentedRoomHistoryByRoomId(@PathVariable String roomId) {
        return ResponseEntity.ok(rentedRoomService.getRentedRoomHistoryByRoomId(roomId));
    }

    @GetMapping("/rented-before/{roomId}")
    public ResponseEntity<Boolean> getRentedRoomBeforeByRoomId(@PathVariable String roomId) {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(rentedRoomService.isUserRentedRoomBefore(userId, roomId));
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<RentalRequest> getRequest(@PathVariable String requestId) {
        return ResponseEntity.ok(rentalRequestCacheService.getRequest(requestId).orElse(null));
    }

    @PostMapping("/request/create")
    public ResponseEntity<RentalRequest> requestRent(@RequestBody CreateRentedRoomRequest createRentedRoomRequest) {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(rentedRoomService.requestRent(userId, createRentedRoomRequest));
    }

    @DeleteMapping("request/cancel/{chatRoomId}")
    public ResponseEntity<Void> cancelRentRequest(@PathVariable String chatRoomId) {
        String userId = this.getUserInfo().getId();
        rentedRoomService.cancelRentRequest(userId, chatRoomId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accept/{chatRoomId}")
    public ResponseEntity<Void> acceptRentedRoom(@PathVariable String chatRoomId) {
        String landlordId = this.getUserInfo().getId();
        rentedRoomService.acceptRent(landlordId, chatRoomId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deny/{chatRoomId}")
    public ResponseEntity<Void> denyRentedRoom(@PathVariable String chatRoomId) {
        String landlordId = this.getUserInfo().getId();
        rentedRoomService.rejectRent(landlordId, chatRoomId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> cancelRent(@PathVariable String roomId) {
        String userId = this.getUserInfo().getId();
        rentedRoomService.cancelRent(userId, roomId);
        return ResponseEntity.ok().build();
    }
}
