package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.CreateRoomRequest;
import com.c2se.roomily.payload.request.UpdateRoomRequest;
import com.c2se.roomily.payload.response.RoomResponse;

import java.math.BigDecimal;
import java.util.List;

public interface RoomService {
    RoomResponse getRoomById(String roomId);
    List<RoomResponse> getRoomsByLandlordId(String landlordId);
    List<RoomResponse> getRoomsByFilter(String city,
                                        String district,
                                        String ward,
                                        String type,
                                        Double minPrice,
                                        Double maxPrice,
                                        Integer minPeople,
                                        Integer maxPeople);
    Boolean createRoom(CreateRoomRequest createRoomRequest, String landlordId);
    RoomResponse updateRoom(String roomId, UpdateRoomRequest updateRoomRequest);
    Boolean deleteRoom(String roomId);
}
