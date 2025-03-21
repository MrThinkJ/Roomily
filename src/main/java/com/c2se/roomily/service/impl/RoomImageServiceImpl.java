package com.c2se.roomily.service.impl;

import com.c2se.roomily.config.StorageConfig;
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
    StorageConfig storageConfig;

    @Override
    public List<String> getRoomImageUrlsByRoomId(String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("Room", "id", roomId)
        );
        List<String> imageNames = roomImageRepository.getRoomImageNamesByRoomId(room.getId());
        return imageNames.stream().map(name -> {
            try {
                return storageService.generatePresignedUrl(storageConfig.getBucketStore(), name);
            } catch (Exception e) {
                return null;
            }
        }).collect(Collectors.toList());
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
            try {
                storageService.putObject(image, storageConfig.getBucketStore(), imageName);
                RoomImage roomImage = RoomImage.builder()
                        .name(imageName)
                        .room(room)
                        .build();
                roomImageRepository.save(roomImage);
            } catch (Exception e) {
                try {
                    storageService.removeObject(storageConfig.getBucketStore(), imageName);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
                storageService.removeObject(storageConfig.getBucketStore(), roomImage.getName());
                roomImageRepository.delete(roomImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String generateImageName(String roomId, String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return roomId + "-" + UUID.randomUUID() + extension;
    }

    private RoomImageResponse mapToResponse(RoomImage roomImage) {
        String url = null;
        try {
            url = storageService.generatePresignedUrl(storageConfig.getBucketStore(), roomImage.getName());
        } catch (Exception e){
            e.printStackTrace();
        }
        return RoomImageResponse.builder()
                .id(roomImage.getId())
                .name(roomImage.getName())
                .url(url)
                .roomId(roomImage.getRoom().getId())
                .createdAt(roomImage.getCreatedDate())
                .build();
    }
}
