package com.c2se.roomily.payload.dao;

import com.c2se.roomily.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomDao {
    private Room room;
    private boolean isSubscribed;
}
