package com.c2se.roomily.service;

import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.Tag;
import com.c2se.roomily.enums.RoomStatus;
import com.c2se.roomily.payload.internal.GooglePlacesTag;
import com.c2se.roomily.payload.request.CreateRoomRequest;
import com.c2se.roomily.payload.request.RoomFilterRequest;
import com.c2se.roomily.payload.request.UpdateRoomRequest;
import com.c2se.roomily.payload.response.RoomResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface RoomService {
    Room getRoomEntityById(String roomId);
    RoomResponse getRoomById(String roomId);
    Set<GooglePlacesTag> getRecommendedTagsByLocation(BigDecimal latitude, BigDecimal longitude);
    void saveRoom(Room room);
    void updateRoomStatus(String roomId, String status);

    boolean isRoomExist(String roomId);

    List<RoomResponse> getRoomsByLandlordId(String landlordId);

    List<RoomResponse> getRoomsByFilter(RoomFilterRequest filterRequest);

    String createRoom(CreateRoomRequest createRoomRequest, String landlordId);

    RoomResponse updateRoom(String roomId, UpdateRoomRequest updateRoomRequest);

    void deleteRoom(String roomId);

    void setRoomFindPartnerOnly(String roomId);

    BigDecimal getAveragePriceAroundRoom(String roomId, Double radius);

    void updateRoomStatusByLandlordId(String landlordId, RoomStatus status);

    RoomResponse getRoomResponse(Room room);
}
