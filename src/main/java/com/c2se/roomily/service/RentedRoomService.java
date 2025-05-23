package com.c2se.roomily.service;

import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.payload.request.CreateRentedRoomRequest;
import com.c2se.roomily.payload.request.RentalRequest;
import com.c2se.roomily.payload.request.UpdateRentedRoomRequest;
import com.c2se.roomily.payload.response.RentedRoomResponse;

import java.util.List;

public interface RentedRoomService {
    RentedRoom getRentedRoomEntityById(String rentedRoomId);

    void saveRentedRoom(RentedRoom rentedRoom);

    RentedRoomResponse getRentedRoomActiveByUserIdOrCoTenantIdAndRoomId(String userId, String roomId);

    List<RentedRoomResponse> getRentedRoomActiveByUserIdOrCoTenantId(String userId);

    List<RentedRoomResponse> getRentedRoomsByLandlordId(String landlordId);

    List<RentedRoomResponse> getRentedRoomHistoryByRoomId(String roomId);

    void deleteRentedRoomNotPaidDepositByRoomId(String roomId);

    RentedRoomResponse getActiveRentedRoomByRoomId(String roomId);

    RentalRequest requestRent(String userId, CreateRentedRoomRequest createRentedRoomRequest);

    void cancelRentRequest(String userId, String chatRoomId);

    void acceptRent(String landlordId, String chatRoomId);

    void rejectRent(String landlordId, String chatRoomId);

    void exitRent(String userId, String rentedRoomId);

    void cancelRent(String userId, String rentedRoomId);

    void updateRentedRoom(String landlordId, String roomId, UpdateRentedRoomRequest updateRentedRoomRequest);

    void mockPublishRoomExpireEvent(String rentedRoomId);

    void mockPublishDebtDateExpireEvent(String rentedRoomId);

    boolean isUserRentedRoomBefore(String userId, String roomId);
//    void deleteRentedRoom(String landlordId, String roomId);
}
