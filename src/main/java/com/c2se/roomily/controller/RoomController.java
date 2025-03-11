package com.c2se.roomily.controller;

import com.c2se.roomily.payload.dao.RoomDao;
import com.c2se.roomily.payload.request.CreateRoomRequest;
import com.c2se.roomily.payload.request.RoomFilterRequest;
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
public class RoomController extends BaseController {
    RoomService roomService;

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable String roomId) {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    @GetMapping("/landlords/{landlordId}")
    public ResponseEntity<List<RoomResponse>> getRoomsByLandlordId(@PathVariable String landlordId) {
        return ResponseEntity.ok(roomService.getRoomsByLandlordId(landlordId));
    }

    @PostMapping("/filter")
    public ResponseEntity<List<RoomResponse>> getRoomsByFilter(@RequestBody RoomFilterRequest filterRequest) {
        return ResponseEntity.ok(
                roomService.getRoomsByFilter(filterRequest)
        );
    }

    @GetMapping("/average-price")
    public ResponseEntity<Double> getAveragePriceAroundRoom(@RequestParam String roomId, @RequestParam Double radius) {
        return ResponseEntity.ok(roomService.getAveragePriceAroundRoom(roomId, radius).doubleValue());
    }

    @GetMapping("/subscribed/nearby")
    public ResponseEntity<List<RoomResponse>> getSubscribedRoomsNearby(@RequestParam double latitude,
                                                                       @RequestParam double longitude,
                                                                       @RequestParam double radiusKm) {
        return ResponseEntity.ok(roomService.getSubscribedRoomsNearby(latitude, longitude, radiusKm));
    }

    @GetMapping("/subscribed/location")
    public ResponseEntity<List<RoomResponse>> getSubscribedRoomsByLocation(@RequestParam String city,
                                                                          @RequestParam String district,
                                                                          @RequestParam String ward) {
        return ResponseEntity.ok(roomService.getSubscribedRoomsByLocation(city, district, ward));
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
