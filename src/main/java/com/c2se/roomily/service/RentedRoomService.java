package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.CreateRentedRoomRequest;
import com.c2se.roomily.payload.request.UpdateRentedRoomRequest;
import com.c2se.roomily.payload.response.RentedRoomResponse;

import java.util.List;

public interface RentedRoomService {
    List<RentedRoomResponse> getRentedRoomsByLandlordId(String landlordId);
    List<RentedRoomResponse> getRentedRoomHistoryByRoomId(String roomId);
    RentedRoomResponse getRentedRoomByRoomId(String roomId);
    List<RentedRoomResponse> getRentedRoomsByUserId(String userId);
    String requestRent(String userId, CreateRentedRoomRequest createRentedRoomRequest);
    void acceptRent(String landlordId, String userId, String privateCode);
    void cancelRent(String landlordId, String roomId);
    void updateRentedRoom(String landlordId, String roomId, UpdateRentedRoomRequest updateRentedRoomRequest);
//    void deleteRentedRoom(String landlordId, String roomId);
}
