package com.c2se.roomily.repository;

import com.c2se.roomily.entity.RentedRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RentedRoomRepository extends JpaRepository<RentedRoom, String> {
    List<RentedRoom> findByRoomId(String roomId);

    RentedRoom findActiveByRoomId(String roomId);

    RentedRoom findByUserIdAndRoomId(String userId, String roomId);

    List<RentedRoom> findByUserId(String userId);

    List<RentedRoom> findByLandlordId(String landlordId);

    List<RentedRoom> findByEndDate(LocalDate endDate);

    List<RentedRoom> findByDebtDate(LocalDate debtDate);
} 