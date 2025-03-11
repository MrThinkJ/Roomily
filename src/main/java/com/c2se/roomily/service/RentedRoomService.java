package com.c2se.roomily.service;

import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.payload.request.CreateRentedRoomRequest;
import com.c2se.roomily.payload.request.UpdateRentedRoomRequest;
import com.c2se.roomily.payload.response.RentedRoomResponse;

import java.util.List;

public interface RentedRoomService {
    RentedRoom getRentedRoomEntityById(String roomId);
    void saveRentedRoom(RentedRoom rentedRoom);
    RentedRoomResponse getRentedRoomActiveByUserIdOrCoTenantIdAndRoomId(String userId, String roomId);
    List<RentedRoomResponse> getRentedRoomActiveByUserIdOrCoTenantId(String userId);
    List<RentedRoomResponse> getRentedRoomsByLandlordId(String landlordId);

    List<RentedRoomResponse> getRentedRoomHistoryByRoomId(String roomId);

    RentedRoomResponse getRentedRoomByRoomId(String roomId);

    List<RentedRoomResponse> getRentedRoomsByUserId(String userId);

    String requestRent(String userId, CreateRentedRoomRequest createRentedRoomRequest);

    void cancelRentRequest(String userId, String privateCode);

    void acceptRent(String landlordId, String privateCode);

    void denyRent(String landlordId, String privateCode);

    void cancelRent(String userId, String roomId);

    void updateRentedRoom(String landlordId, String roomId, UpdateRentedRoomRequest updateRentedRoomRequest);
//    void deleteRentedRoom(String landlordId, String roomId);
}
