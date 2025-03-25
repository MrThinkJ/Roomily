package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.RentedRoomActivity;
import com.c2se.roomily.enums.RentedRoomActivityType;
import com.c2se.roomily.payload.request.CreateRentedRoomActivityRequest;
import com.c2se.roomily.payload.response.RentedRoomActivityResponse;
import com.c2se.roomily.repository.RentedRoomActivityRepository;
import com.c2se.roomily.security.CustomUserDetails;
import com.c2se.roomily.service.RentedRoomActivityService;
import com.c2se.roomily.service.RentedRoomOperationsService;
import com.c2se.roomily.service.RentedRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentedRoomActivityServiceImpl implements RentedRoomActivityService {
    private final RentedRoomActivityRepository rentedRoomActivityRepository;
    private final RentedRoomOperationsService rentedRoomOperationsService;

    @Override
    public void createRentedRoomActivity(CreateRentedRoomActivityRequest createRentedRoomActivityRequest) {
        RentedRoom rentedRoom = rentedRoomOperationsService.getRentedRoomById(
                createRentedRoomActivityRequest.getRentedRoomId());
        RentedRoomActivity rentedRoomActivity = RentedRoomActivity.builder()
                .rentedRoom(rentedRoom)
                .message(createRentedRoomActivityRequest.getMessage())
                .build();
        rentedRoomActivityRepository.save(rentedRoomActivity);
    }

    @Override
    public List<RentedRoomActivityResponse> getRentedRoomActivitiesByRentedRoomId(String rentedRoomId, String pivotId,
                                                                                  String timestamp, int limit) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        RentedRoom rentedRoom = rentedRoomOperationsService.getRentedRoomById(rentedRoomId);
        if (!rentedRoom.getUser().getId().equals(customUserDetails.getId()) &&
            !rentedRoom.getRoom().getLandlord().getId().equals(customUserDetails.getId()) &&
             rentedRoom.getCoTenants().stream().noneMatch(coTenant -> coTenant.getId().equals(customUserDetails.getId()))) {
            throw new RuntimeException("You are not authorized to view this rented room activities");
        }
        pivotId = pivotId != null ? pivotId : "zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz";
        LocalDateTime time = timestamp != null ? LocalDateTime.parse(timestamp) : LocalDateTime.now().plusDays(1);
        return rentedRoomActivityRepository.findByRentedRoomId(rentedRoomId, pivotId, time, limit)
                .stream()
                .map(rentedRoomActivity ->
                             RentedRoomActivityResponse.builder()
                                     .id(rentedRoomActivity.getId())
                                     .rentedRoomId(rentedRoomActivity.getRentedRoom().getId())
                                     .createdAt(rentedRoomActivity.getCreatedAt())
                                     .message(rentedRoomActivity.getMessage())
                                     .build())
                .toList();
    }
}
