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
public class RentedRoomController extends BaseController{
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

    @PostMapping("/accept/user/{userId}/private-code/{privateCode}")
    public void acceptRentedRoom(@PathVariable String userId, @PathVariable String privateCode) {
        String landlordId = this.getUserInfo().getId();
        rentedRoomService.acceptRent(landlordId, userId, privateCode);
    }

}
