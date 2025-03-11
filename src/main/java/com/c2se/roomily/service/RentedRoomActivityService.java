package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.CreateRentedRoomActivityRequest;
import com.c2se.roomily.payload.response.RentedRoomActivityResponse;

import java.util.List;

public interface RentedRoomActivityService {
    void createRentedRoomActivity(CreateRentedRoomActivityRequest createRentedRoomActivityRequest);

    List<RentedRoomActivityResponse> getRentedRoomActivitiesByRentedRoomId(String rentedRoomId, String pivotId,
                                                                           String timestamp, int limit);
}
