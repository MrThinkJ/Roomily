package com.c2se.roomily.repository;

import com.c2se.roomily.entity.RentedRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentedRoomRepository extends JpaRepository<RentedRoom, String> {
} 