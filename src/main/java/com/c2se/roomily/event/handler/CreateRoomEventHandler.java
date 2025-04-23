package com.c2se.roomily.event.handler;

import com.c2se.roomily.config.RabbitMQConfig;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.event.pojo.CreateRoomEvent;
import com.c2se.roomily.payload.internal.GooglePlacesResponseResult;
import com.c2se.roomily.payload.internal.GooglePlacesTag;
import com.c2se.roomily.service.RoomService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateRoomEventHandler {
    private final RoomService roomService;
    private final RabbitTemplate rabbitTemplate;

    @EventListener
    @Async
    @Transactional
    public void handle(CreateRoomEvent event) {
        try {
            Room room = roomService.getRoomEntityById(event.getRoomId());
            room.setNearbyAmenities(processNearByAmenities(room.getLatitude(), room.getLongitude()));
            roomService.saveRoom(room);
            Map<String, String> body = new HashMap<>();
            body.put("room_id", room.getId());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,
                                          RabbitMQConfig.ROOM_ROUTING_KEY,
                                          body);
            System.out.println("Handling CreateRoomEvent for roomId: " + event.getRoomId());
        } catch (Exception e) {
            System.err.println("Error handling CreateRoomEvent: " + e.getMessage());
        }
    }

    private String processNearByAmenities(double latitude, double longitude) {
        Set<GooglePlacesTag> recommendedTags = roomService.getRecommendedTagsByLocation(BigDecimal.valueOf(latitude),
                                                                                        BigDecimal.valueOf(longitude));
        StringBuilder nearByAmenities = new StringBuilder();
        for (GooglePlacesTag googlePlacesTag : recommendedTags){
            nearByAmenities.append(googlePlacesTag.getTagName()).append(":");
            nearByAmenities.append(googlePlacesTag.getDistance()).append(":");
            nearByAmenities.append(googlePlacesTag.getLatitude()).append(":");
            nearByAmenities.append(googlePlacesTag.getLongitude()).append(",");
        }
        return nearByAmenities.toString();
    }
}
