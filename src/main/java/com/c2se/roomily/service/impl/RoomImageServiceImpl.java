package com.c2se.roomily.service.impl;

import com.c2se.roomily.config.StorageConfiguration;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.RoomImage;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.response.RoomImageResponse;
import com.c2se.roomily.repository.RoomImageRepository;
import com.c2se.roomily.repository.RoomRepository;
import com.c2se.roomily.service.RoomImageService;
import com.c2se.roomily.service.StorageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoomImageServiceImpl implements RoomImageService {
    RoomImageRepository roomImageRepository;
    RoomRepository roomRepository;
    StorageService storageService;
    StorageConfiguration storageConfiguration;
    @Override
    public List<String> getRoomImageUrlsByRoomId(String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("Room", "id", roomId)
        );
        return roomImageRepository.findUrlsByRoomId(room.getId());
    }

    @Override
    public List<RoomImageResponse> getRoomImagesByRoomId(String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("Room", "id", roomId)
        );
        List<RoomImage> roomImages = roomImageRepository.findByRoomId(room.getId());
        return roomImages.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadRoomImages(String roomId, List<MultipartFile> images) {
        Room room = roomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("Room", "id", roomId)
        );

        for (MultipartFile image : images) {
            String imageName = generateImageName(roomId, image.getOriginalFilename());
            try{
                storageService.putObject(image, storageConfiguration.getBucketStore(), imageName);
                String imageUrl = storageService.generatePresignedUrl(storageConfiguration.getBucketStore(), imageName);
                RoomImage roomImage = RoomImage.builder()
                        .name(imageName)
                        .room(room)
                        .url(imageUrl)
                        .build();
                roomImageRepository.save(roomImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteImages(String userId, String roomId, List<String> imageIds) {
        Room room = roomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("Room", "id", roomId)
        );
        if (!room.getLandlord().getId().equals(userId)) {
            throw new APIException(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
        }
        List<RoomImage> roomImages = roomImageRepository.findByRoomIdAndIdIn(roomId, imageIds);
        for (RoomImage roomImage : roomImages) {
            try {
                storageService.removeObject(storageConfiguration.getBucketStore(), roomImage.getName());
                roomImageRepository.delete(roomImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String generateImageName(String roomId, String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return roomId+"-"+UUID.randomUUID()+extension;
    }

    private RoomImageResponse mapToResponse(RoomImage roomImage) {
        return RoomImageResponse.builder()
                .id(roomImage.getId())
                .name(roomImage.getName())
                .url(roomImage.getUrl())
                .roomId(roomImage.getRoom().getId())
                .createdAt(roomImage.getCreatedDate().toString())
                .build();
    }
}
