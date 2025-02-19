package com.c2se.roomily.controller;

import com.c2se.roomily.payload.response.RoomImageResponse;
import com.c2se.roomily.service.RoomImageService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/room-images")
public class RoomImageController extends BaseController{
    RoomImageService roomImageService;
    @GetMapping("urls/rooms/{roomId}")
    public List<String> getRoomImageUrlsByRoomId(@PathVariable String roomId) {
        return roomImageService.getRoomImageUrlsByRoomId(roomId);
    }

    @GetMapping("rooms/{roomId}")
    public List<RoomImageResponse> getRoomImagesByRoomId(@PathVariable String roomId) {
        return roomImageService.getRoomImagesByRoomId(roomId);
    }

    @PostMapping("rooms/{roomId}")
    public void uploadRoomImages(@PathVariable String roomId, @RequestParam("images") List<MultipartFile> images) {
        roomImageService.uploadRoomImages(roomId, images);
    }

    @DeleteMapping("rooms/{roomId}")
    public void deleteImages(@PathVariable String roomId, @RequestBody List<String> imageIds) {
        String userId = this.getUserInfo().getId();
        roomImageService.deleteImages(roomId, userId, imageIds);
    }
}
