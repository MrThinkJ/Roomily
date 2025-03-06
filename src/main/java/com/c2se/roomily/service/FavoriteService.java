package com.c2se.roomily.service;

import com.c2se.roomily.payload.response.RoomResponse;

import java.util.List;

public interface FavoriteService {
    boolean toggleFavorite(String userId, String roomId);

    boolean isFavorite(String userId, String roomId);

    List<RoomResponse> getFavoriteRooms(String userId);

    int countFavoriteRooms(String userId);

    int countFavoriteByRoomId(String roomId);
}
