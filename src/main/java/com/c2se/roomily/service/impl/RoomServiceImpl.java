package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.Tag;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.enums.RoomStatus;
import com.c2se.roomily.enums.RoomType;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.CreateRoomRequest;
import com.c2se.roomily.payload.request.UpdateRoomRequest;
import com.c2se.roomily.payload.response.RoomResponse;
import com.c2se.roomily.repository.RoomRepository;
import com.c2se.roomily.repository.TagRepository;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.service.RoomService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoomServiceImpl implements RoomService {
    RoomRepository roomRepository;
    TagRepository tagRepository;
    UserRepository userRepository;
    @Override
    public RoomResponse getRoomById(String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("Room", "id", roomId)
        );
        return this.mapToRoomResponse(room);
    }

    @Override
    public List<RoomResponse> getRoomsByLandlordId(String landlordId) {
        List<Room> rooms = roomRepository.findByLandlordId(landlordId);
        return rooms.stream().map(this::mapToRoomResponse).toList();
    }

    @Override
    public List<RoomResponse> getRoomsByFilter(String city,
                                               String district,
                                               String ward, String type,
                                               Double minPrice, Double maxPrice,
                                               Integer minPeople, Integer maxPeople) {
        RoomType roomType = type.isEmpty() ? null : RoomType.valueOf(type);
        List<Room> rooms = roomRepository.findByFilter(city, district, ward, roomType,
                minPrice, maxPrice, minPeople, maxPeople);
        return rooms.stream().map(this::mapToRoomResponse).toList();
    }

    @Override
    public Boolean createRoom(CreateRoomRequest createRoomRequest, String landlordId) {
        User landlord = userRepository.findById(landlordId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", landlordId)
        );

        List<Tag> tags = tagRepository.findByIdIn(createRoomRequest.getTagIds());
        if (tags.size() != createRoomRequest.getTagIds().size()) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "Some tags are not found");
        }
        Room room = Room.builder()
                .title(createRoomRequest.getTitle())
                .description(createRoomRequest.getDescription())
                .address(createRoomRequest.getAddress())
                .status(RoomStatus.AVAILABLE)
                .price(BigDecimal.valueOf(Double.parseDouble(createRoomRequest.getPrice())))
                .latitude(createRoomRequest.getLatitude())
                .longitude(createRoomRequest.getLongitude())
                .city(createRoomRequest.getCity())
                .district(createRoomRequest.getDistrict())
                .ward(createRoomRequest.getWard())
                .electricPrice(BigDecimal.valueOf(Double.parseDouble(createRoomRequest.getElectricPrice())))
                .waterPrice(BigDecimal.valueOf(Double.parseDouble(createRoomRequest.getWaterPrice())))
                .type(RoomType.valueOf(createRoomRequest.getType()))
                .nearbyAmenities(getNearbyAmenitiesString(createRoomRequest.getLatitude(),
                        createRoomRequest.getLongitude()))
                .maxPeople(createRoomRequest.getMaxPeople())
                .landlord(landlord)
                .deposit(createRoomRequest.getDeposit())
                .tags(new HashSet<>(tags))
                .squareMeters(createRoomRequest.getSquareMeters())
                .build();
        roomRepository.save(room);
        return true;
    }

    @Override
    public RoomResponse updateRoom(String roomId, UpdateRoomRequest updateRoomRequest) {
        Room room = roomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("Room", "id", roomId)
        );
        room.setTitle(updateRoomRequest.getTitle());
        room.setDescription(updateRoomRequest.getDescription());
        room.setAddress(updateRoomRequest.getAddress());
        room.setStatus(RoomStatus.valueOf(updateRoomRequest.getStatus()));
        room.setPrice(BigDecimal.valueOf(Double.parseDouble(updateRoomRequest.getPrice())));
        room.setLatitude(updateRoomRequest.getLatitude());
        room.setLongitude(updateRoomRequest.getLongitude());
        room.setCity(updateRoomRequest.getCity());
        room.setDistrict(updateRoomRequest.getDistrict());
        room.setWard(updateRoomRequest.getWard());
        room.setElectricPrice(BigDecimal.valueOf(Double.parseDouble(updateRoomRequest.getElectricPrice())));
        room.setWaterPrice(BigDecimal.valueOf(Double.parseDouble(updateRoomRequest.getWaterPrice())));
        room.setType(RoomType.valueOf(updateRoomRequest.getType()));
        room.setMaxPeople(updateRoomRequest.getMaxPeople());
        room.setTags(updateRoomRequest.getTags().stream()
                .map(tag -> Tag.builder().name(tag).build())
                .collect(Collectors.toSet()));
        room.setSquareMeters(updateRoomRequest.getSquareMeters());
        room.setDeposit(updateRoomRequest.getDeposit());
        Room updatedRoom = roomRepository.save(room);
        return mapToRoomResponse(updatedRoom);
    }

    @Override
    public Boolean deleteRoom(String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("Room", "id", roomId)
        );
        room.setStatus(RoomStatus.DELETED);
        roomRepository.save(room);
        return true;
    }

    private String getNearbyAmenitiesString(Double latitude, Double longitude) {
        // TODO: Implement this method to get nearby amenities from latitude and longitude by calling Google Maps API
        return "";
    }

    private RoomResponse mapToRoomResponse(Room room) {
        return getRoomResponse(room);
    }

    static RoomResponse getRoomResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .title(room.getTitle())
                .description(room.getDescription())
                .address(room.getAddress())
                .status(room.getStatus().name())
                .price(room.getPrice())
                .latitude(room.getLatitude())
                .longitude(room.getLongitude())
                .city(room.getCity())
                .district(room.getDistrict())
                .ward(room.getWard())
                .electricPrice(room.getElectricPrice())
                .waterPrice(room.getWaterPrice())
                .type(room.getType().name())
                .nearbyAmenities(room.getNearbyAmenities())
                .maxPeople(room.getMaxPeople())
                .landlordId(room.getLandlord().getId())
                .tags(room.getTags())
                .deposit(room.getDeposit())
                .createdAt(room.getCreatedAt().toString())
                .updatedAt(room.getUpdatedAt().toString())
                .squareMeters(room.getSquareMeters())
                .build();
    }
}
