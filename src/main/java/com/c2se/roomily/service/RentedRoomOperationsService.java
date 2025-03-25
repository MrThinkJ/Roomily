package com.c2se.roomily.service;

import com.c2se.roomily.entity.RentedRoom;

public interface RentedRoomOperationsService {
    RentedRoom getRentedRoomById(String id);
    void saveRentedRoom(RentedRoom rentedRoom);
}
