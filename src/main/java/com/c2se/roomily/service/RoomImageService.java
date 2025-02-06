package com.c2se.roomily.service;

import com.c2se.roomily.payload.response.RoomImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RoomImageService {
    List<String> getRoomImageUrlsByRoomId(String roomId);
    List<RoomImageResponse> getRoomImagesByRoomId(String roomId);
    void uploadRoomImages(String roomId, List<MultipartFile> images);
    void deleteImages(String userId, String roomId, List<String> imageIds);
}
