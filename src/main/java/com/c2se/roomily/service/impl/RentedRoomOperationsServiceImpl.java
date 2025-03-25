package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.repository.RentedRoomRepository;
import com.c2se.roomily.service.RentedRoomOperationsService;
import com.c2se.roomily.service.RentedRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentedRoomOperationsServiceImpl implements RentedRoomOperationsService {
    private final RentedRoomRepository rentedRoomRepository;

    @Override
    public RentedRoom getRentedRoomById(String id) {
        return rentedRoomRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("RentedRoom", "id", id)
        );
    }

    @Override
    public void saveRentedRoom(RentedRoom rentedRoom) {
        rentedRoomRepository.save(rentedRoom);
    }
}
