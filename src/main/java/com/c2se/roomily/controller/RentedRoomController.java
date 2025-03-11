package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.CreateRentedRoomRequest;
import com.c2se.roomily.payload.response.RentedRoomResponse;
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

    @GetMapping
    public ResponseEntity<List<RentedRoomResponse>> getRentedRoomsByUserId() {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(rentedRoomService.getRentedRoomActiveByUserIdOrCoTenantId(userId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<RentedRoomResponse>> getActiveRentedRoomsByUserId() {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(rentedRoomService.getRentedRoomActiveByUserIdOrCoTenantId(userId));
    }

    @GetMapping("/landlord/{landlordId}")
    public ResponseEntity<List<RentedRoomResponse>> getRentedRoomsByLandlordId(@PathVariable String landlordId) {
        return ResponseEntity.ok(rentedRoomService.getRentedRoomsByLandlordId(landlordId));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<RentedRoomResponse> getRentedRoomByRoomId(@PathVariable String roomId) {
        return ResponseEntity.ok(rentedRoomService.getRentedRoomByRoomId(roomId));
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

    @PostMapping("/request/create")
    public ResponseEntity<String> createRentedRoom(@RequestBody CreateRentedRoomRequest createRentedRoomRequest) {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(rentedRoomService.requestRent(userId, createRentedRoomRequest));
    }

    @DeleteMapping("request/cancel")
    public ResponseEntity<Void> cancelRentRequest(@RequestParam String roomId) {
        String userId = this.getUserInfo().getId();
        rentedRoomService.cancelRentRequest(userId, roomId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accept")
    public ResponseEntity<Void> acceptRentedRoom(@RequestBody String privateCode) {
        String landlordId = this.getUserInfo().getId();
        rentedRoomService.acceptRent(landlordId, privateCode);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deny")
    public ResponseEntity<Void> denyRentedRoom(@RequestBody String privateCode) {
        String landlordId = this.getUserInfo().getId();
        rentedRoomService.denyRent(landlordId, privateCode);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> cancelRent(@PathVariable String roomId) {
        String userId = this.getUserInfo().getId();
        rentedRoomService.cancelRent(userId, roomId);
        return ResponseEntity.ok().build();
    }
}
