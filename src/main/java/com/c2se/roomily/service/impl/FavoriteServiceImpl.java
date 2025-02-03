package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.Favorite;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.response.RoomResponse;
import com.c2se.roomily.repository.FavoriteRepository;
import com.c2se.roomily.repository.RoomRepository;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.c2se.roomily.service.impl.RoomServiceImpl.getRoomResponse;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    FavoriteRepository favoriteRepository;
    UserRepository userRepository;
    RoomRepository roomRepository;
    @Override
    public boolean toggleFavorite(String userId, String roomId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", userId));
        Room room = roomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("Room", "id", roomId));
        if (favoriteRepository.existsByUserIdAndRoomId(userId, roomId))
            favoriteRepository.deleteByUserIdAndRoomId(userId, roomId);
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
        return  favoriteRepository.existsByUserIdAndRoomId(userId, roomId);
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
