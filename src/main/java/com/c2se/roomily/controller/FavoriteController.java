package com.c2se.roomily.controller;

import com.c2se.roomily.payload.response.RoomResponse;
import com.c2se.roomily.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/favorites")
public class FavoriteController extends BaseController{
    FavoriteService favoriteService;
    @PatchMapping("/{roomId}")
    public ResponseEntity<Boolean> toggleFavorite(@PathVariable String roomId) {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(favoriteService.toggleFavorite(userId, roomId));
    }
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getFavoriteRooms() {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(favoriteService.getFavoriteRooms(userId));
    }
    @GetMapping("/count")
    public ResponseEntity<Integer> countFavoriteRooms() {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(favoriteService.countFavoriteRooms(userId));
    }
    @GetMapping("/count/{roomId}")
    public ResponseEntity<Integer> countFavoriteByRoomId(@PathVariable String roomId) {
        return ResponseEntity.ok(favoriteService.countFavoriteByRoomId(roomId));
    }
}
