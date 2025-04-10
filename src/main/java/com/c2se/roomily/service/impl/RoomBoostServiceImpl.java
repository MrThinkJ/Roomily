package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.RoomBoost;
import com.c2se.roomily.payload.response.RoomBoostResponse;
import com.c2se.roomily.repository.RoomBoostRepository;
import com.c2se.roomily.service.RoomBoostService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class RoomBoostServiceImpl implements RoomBoostService {
    
    private final RoomBoostRepository roomBoostRepository;
    
    // Boost factors based on boost level (1-5)
    private static final double[] BOOST_FACTORS = {1.0, 1.2, 1.5, 2.0, 3.0, 5.0};
    
    @Override
    public List<RoomBoostResponse> getActiveRoomBoosts() {
        List<RoomBoost> boosts = roomBoostRepository.findActiveBoosts(LocalDateTime.now());
        return boosts.stream()
                .map(this::mapToRoomBoostResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<String> getBoostedRoomIds() {
        return roomBoostRepository.findActiveBoostedRoomIds(LocalDateTime.now());
    }
    
    @Override
    public List<String> getBoostedRoomIdsByLocation(String city, String district, String ward) {
//        return roomBoostRepository.findActiveBoostedRoomIdsByLocation(city, district, ward, LocalDateTime.now());
        return null;
    }
    
    @Override
    public boolean isRoomBoosted(String roomId) {
        return roomBoostRepository.isRoomBoosted(roomId, LocalDateTime.now());
    }
    
    @Override
    public Map<String, Integer> getRoomBoostLevels() {
        List<RoomBoost> activeBoosts = roomBoostRepository.findActiveBoosts(LocalDateTime.now());
        Map<String, Integer> boostLevels = new HashMap<>();
        
        for (RoomBoost boost : activeBoosts) {
            String roomId = boost.getRoom().getId();
            // If a room has multiple active boosts, use the highest boost level
            boostLevels.compute(roomId, (key, existingLevel) -> 
                existingLevel == null ? boost.getBoostLevel() : Math.max(existingLevel, boost.getBoostLevel()));
        }
        
        return boostLevels;
    }
    
    @Override
    public Map<String, Double> applyBoostFactors(Map<String, Double> roomScores) {
        if (roomScores == null || roomScores.isEmpty()) {
            return roomScores;
        }
        
        Map<String, Integer> boostLevels = getRoomBoostLevels();
        Map<String, Double> boostedScores = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : roomScores.entrySet()) {
            String roomId = entry.getKey();
            Double score = entry.getValue();
            
            // Apply boost factor if the room is boosted
            if (boostLevels.containsKey(roomId)) {
                int boostLevel = boostLevels.get(roomId);
                double boostFactor = getBoostFactorByLevel(boostLevel);
                boostedScores.put(roomId, score * boostFactor);
            } else {
                boostedScores.put(roomId, score);
            }
        }
        
        return boostedScores;
    }
    
    @Override
    public double getBoostFactorForRoom(String roomId) {
        if (!isRoomBoosted(roomId)) {
            return 1.0; // No boost
        }
        
        // Get the highest boost level for this room
        List<RoomBoost> boosts = roomBoostRepository.findActiveBoostsByRoomId(roomId, LocalDateTime.now());
        int highestBoostLevel = boosts.stream()
                .mapToInt(RoomBoost::getBoostLevel)
                .max()
                .orElse(0);
        
        return getBoostFactorByLevel(highestBoostLevel);
    }

    private double getBoostFactorByLevel(int level) {
        if (level < 1 || level >= BOOST_FACTORS.length) {
            return 1.0; // Default no boost
        }
        return BOOST_FACTORS[level];
    }

    private RoomBoostResponse mapToRoomBoostResponse(RoomBoost boost) {
        return RoomBoostResponse.builder()
                .id(boost.getId())
                .roomId(boost.getRoom().getId())
                .roomTitle(boost.getRoom().getTitle())
                .roomAddress(boost.getRoom().getAddress())
                .userId(boost.getUser().getId())
                .userName(boost.getUser().getFullName())
                .startDate(boost.getStartDate())
                .endDate(boost.getEndDate())
                .creditsUsed(boost.getCreditsUsed())
                .active(boost.isActive())
                .boostLevel(boost.getBoostLevel())
                .radiusKm(boost.getRadiusKm())
                .createdAt(boost.getCreatedAt())
                .updatedAt(boost.getUpdatedAt())
                .build();
    }

    @Override
    public List<Boolean> isRoomBoosted(List<String> roomIds) {
        throw new UnsupportedOperationException("Unimplemented method 'isRoomBoosted'");
    }
}
