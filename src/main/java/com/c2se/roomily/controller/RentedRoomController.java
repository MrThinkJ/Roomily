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

    @GetMapping("/landlord/{landlordId}")
    public List<RentedRoomResponse> getRentedRoomsByLandlordId(@PathVariable String landlordId) {
        return rentedRoomService.getRentedRoomsByLandlordId(landlordId);
    }

    @GetMapping("/room/{roomId}")
    public RentedRoomResponse getRentedRoomByRoomId(@PathVariable String roomId) {
        return rentedRoomService.getRentedRoomByRoomId(roomId);
    }

    @GetMapping("/user/{userId}")
    public List<RentedRoomResponse> getRentedRoomsByUserId(@PathVariable String userId) {
        return rentedRoomService.getRentedRoomsByUserId(userId);
    }

    @GetMapping("/history/{roomId}")
    public List<RentedRoomResponse> getRentedRoomHistoryByRoomId(@PathVariable String roomId) {
        return rentedRoomService.getRentedRoomHistoryByRoomId(roomId);
    }

    @PostMapping("/request")
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

    @DeleteMapping("/cancel/{roomId}")
    public ResponseEntity<Void> cancelRent(@PathVariable String roomId) {
        String userId = this.getUserInfo().getId();
        rentedRoomService.cancelRent(userId, roomId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accept")
    public ResponseEntity<Void> acceptRentedRoom(@RequestBody String privateCode){
        String landlordId = this.getUserInfo().getId();
        rentedRoomService.acceptRent(landlordId, privateCode);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deny")
    public ResponseEntity<Void> denyRentedRoom(@RequestBody String privateCode){
        String landlordId = this.getUserInfo().getId();
        rentedRoomService.denyRent(landlordId, privateCode);
        return ResponseEntity.ok().build();
    }

}
