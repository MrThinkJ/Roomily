package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.CreateRoomRequest;
import com.c2se.roomily.payload.request.UpdateRoomRequest;
import com.c2se.roomily.payload.response.RoomResponse;
import com.c2se.roomily.service.RoomService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/rooms")
public class RoomController extends BaseController{
    RoomService roomService;

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable String roomId) {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    @GetMapping("/landlords/{landlordId}")
    public ResponseEntity<List<RoomResponse>> getRoomsByLandlordId(@PathVariable String landlordId) {
        return ResponseEntity.ok(roomService.getRoomsByLandlordId(landlordId));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<RoomResponse>> getRoomsByFilter(@RequestParam(defaultValue = "") String city,
                                                               @RequestParam(defaultValue = "") String district,
                                                               @RequestParam(defaultValue = "") String ward,
                                                               @RequestParam(defaultValue = "") String type,
                                                               @RequestParam(defaultValue = "0") Double minPrice,
                                                               @RequestParam(defaultValue = "99999999999") Double maxPrice,
                                                               @RequestParam(defaultValue = "0") Integer minPeople,
                                                               @RequestParam(defaultValue = "10") Integer maxPeople) {
        return ResponseEntity.ok(roomService.getRoomsByFilter(city, district, ward, type, minPrice, maxPrice, minPeople, maxPeople));
    }

    @PostMapping
    public ResponseEntity<Boolean> createRoom(@RequestBody CreateRoomRequest createRoomRequest) {
        String landlordId = this.getUserInfo().getId();
        return ResponseEntity.ok(roomService.createRoom(createRoomRequest, landlordId));
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable String roomId,
                                                   @RequestBody UpdateRoomRequest updateRoomRequest) {
        return ResponseEntity.ok(roomService.updateRoom(roomId, updateRoomRequest));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Boolean> deleteRoom(@PathVariable String roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.ok(true);
    }
}
