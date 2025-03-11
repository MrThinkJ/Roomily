package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.RentedRoomActivity;
import com.c2se.roomily.enums.RentedRoomActivityType;
import com.c2se.roomily.payload.request.CreateRentedRoomActivityRequest;
import com.c2se.roomily.payload.response.RentedRoomActivityResponse;
import com.c2se.roomily.repository.RentedRoomActivityRepository;
import com.c2se.roomily.service.RentedRoomActivityService;
import com.c2se.roomily.service.RentedRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RentedRoomActivityServiceImpl implements RentedRoomActivityService {
    private final RentedRoomActivityRepository rentedRoomActivityRepository;
    private final RentedRoomService rentedRoomService;

    @Override
    public void createRentedRoomActivity(CreateRentedRoomActivityRequest createRentedRoomActivityRequest) {
        RentedRoom rentedRoom = rentedRoomService.getRentedRoomEntityById(createRentedRoomActivityRequest.getRentedRoomId());
        RentedRoomActivity rentedRoomActivity = RentedRoomActivity.builder()
                .rentedRoom(rentedRoom)
                .activityType(RentedRoomActivityType.valueOf(createRentedRoomActivityRequest.getActivityType()))
                .message(createRentedRoomActivityRequest.getMessage())
                .build();
        rentedRoomActivityRepository.save(rentedRoomActivity);
    }

    @Override
    public List<RentedRoomActivityResponse> getRentedRoomActivitiesByRentedRoomId(String rentedRoomId, String pivotId,
                                                                                  String timestamp, int limit) {
        return rentedRoomActivityRepository.findByRentedRoomId(rentedRoomId, pivotId, timestamp, limit)
                .stream()
                .map(rentedRoomActivity ->
                        RentedRoomActivityResponse.builder()
                                .id(rentedRoomActivity.getId())
                                .rentedRoomId(rentedRoomActivity.getRentedRoom().getId())
                                .activityType(rentedRoomActivity.getActivityType().name())
                                .createdAt(rentedRoomActivity.getCreatedAt())
                                .message(rentedRoomActivity.getMessage())
                                .build())
                .toList();
    }
}
