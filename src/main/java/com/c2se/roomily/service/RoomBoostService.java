package com.c2se.roomily.service;

import com.c2se.roomily.payload.response.RoomBoostResponse;

import java.util.List;
import java.util.Map;

public interface RoomBoostService {
    List<RoomBoostResponse> getActiveRoomBoosts();
    
    List<String> getBoostedRoomIds();
    
    List<String> getBoostedRoomIdsByLocation(String city, String district, String ward);
    
    boolean isRoomBoosted(String roomId);

    List<Boolean> isRoomBoosted(List<String> roomIds);
    
    Map<String, Integer> getRoomBoostLevels();
    
    Map<String, Double> applyBoostFactors(Map<String, Double> roomScores);
    
    double getBoostFactorForRoom(String roomId);
}
