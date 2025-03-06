package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.Favorite;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.payload.response.RoomResponse;
import com.c2se.roomily.repository.FavoriteRepository;
import com.c2se.roomily.service.FavoriteService;
import com.c2se.roomily.service.RoomService;
import com.c2se.roomily.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.c2se.roomily.service.impl.RoomServiceImpl.getRoomResponse;

@Service
@AllArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    FavoriteRepository favoriteRepository;
    UserService userService;
    RoomService roomService;

    @Override
    public boolean toggleFavorite(String userId, String roomId) {
        User user = userService.getUserEntity(userId);
        Room room = roomService.getRoomEntityById(roomId);
        if (favoriteRepository.existsByUserIdAndRoomId(userId, roomId))
            favoriteRepository.toggleByUserIdAndRoomId(userId, roomId);
        else {
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setRoom(room);
            favoriteRepository.save(favorite);
        }
        return true;
    }

    @Override
    public boolean isFavorite(String userId, String roomId) {
        return favoriteRepository.existsByUserIdAndRoomId(userId, roomId);
    }

    @Override
    public List<RoomResponse> getFavoriteRooms(String userId) {
        List<Favorite> favorites = favoriteRepository.findAllByUserId(userId);
        List<Room> rooms = favorites.stream().map(Favorite::getRoom).toList();
        return rooms.stream().map(this::mapToRoomResponse).collect(Collectors.toList());
    }

    @Override
    public int countFavoriteRooms(String userId) {
        return favoriteRepository.countByUserId(userId);
    }

    @Override
    public int countFavoriteByRoomId(String roomId) {
        return favoriteRepository.countByRoomId(roomId);
    }

    private RoomResponse mapToRoomResponse(Room room) {
        return getRoomResponse(room);
    }


}
