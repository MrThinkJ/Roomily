package com.c2se.roomily.repository;

import com.c2se.roomily.entity.BillLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillLogRepository extends JpaRepository<BillLog, String> {
    List<BillLog> findByRoomId(String roomId);
    List<BillLog> findByRentedRoomId(String rentedRoomId);
    @Query("SELECT b FROM BillLog b WHERE b.roomId = :roomId AND b.billStatus != 'PAID' AND b.billStatus != 'CANCELLED'")
    List<BillLog> findActiveBillLogByRoomId(String roomId);
    @Query("SELECT b FROM BillLog b WHERE b.rentedRoom.id = :rentedRoomId AND b.billStatus != 'PAID' AND b.billStatus != 'CANCELLED'")
    List<BillLog> findActiveBillLogByRentedRoomId(String rentedRoomId);
}
