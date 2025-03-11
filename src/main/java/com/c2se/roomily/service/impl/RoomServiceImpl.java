package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.Tag;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.enums.RoomStatus;
import com.c2se.roomily.enums.RoomType;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.dao.RoomDao;
import com.c2se.roomily.payload.internal.FilterParameters;
import com.c2se.roomily.payload.request.CreateRoomRequest;
import com.c2se.roomily.payload.request.RoomFilterRequest;
import com.c2se.roomily.payload.request.UpdateRoomRequest;
import com.c2se.roomily.payload.response.RoomResponse;
import com.c2se.roomily.repository.RoomRepository;
import com.c2se.roomily.service.RoomService;
import com.c2se.roomily.service.SubscriptionService;
import com.c2se.roomily.service.TagService;
import com.c2se.roomily.service.UserService;
import com.c2se.roomily.util.AppConstants;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final TagService tagService;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final BigDecimal DEFAULT_MIN_PRICE = BigDecimal.ZERO;
    private final BigDecimal DEFAULT_MAX_PRICE = BigDecimal.valueOf(1_000_000_000);
    private final int DEFAULT_MIN_PEOPLE = 0;
    private final int DEFAULT_MAX_PEOPLE = 100;
    private final int DEFAULT_LIMIT = 20;

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
                .deposit(room.getRentalDeposit())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .squareMeters(room.getSquareMeters())
                .build();
    }

    @Override
    public Room getRoomEntityById(String roomId) {
        return roomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("Room", "Id", roomId)
        );
    }

    @Override
    public RoomResponse getRoomById(String roomId) {
        Room room = getRoomEntityById(roomId);
        return this.mapToRoomResponse(room);
    }

    @Override
    public void updateRoomStatus(String roomId, String status) {
        Room room = getRoomEntityById(roomId);
        room.setStatus(RoomStatus.valueOf(status));
        roomRepository.save(room);
    }

    @Override
    public boolean isRoomExist(String roomId) {
        return roomRepository.existsById(roomId);
    }

    @Override
    public List<RoomResponse> getRoomsByLandlordId(String landlordId) {
        List<Room> rooms = roomRepository.findByLandlordId(landlordId);
        return rooms.stream().map(this::mapToRoomResponse).collect(Collectors.toList());
    }

    @Override
    public List<RoomResponse> getRoomsByFilter(RoomFilterRequest request) {
        List<String> subscribedLandlordIds = subscriptionService.getLandlordsWithActiveSubscriptions();
        FilterParameters filterParameters = normalizeRoomFilterRequest(request);

        List<RoomDao> rooms = roomRepository.findRoomsWithCursor(
                filterParameters.getCity(),
                filterParameters.getDistrict(),
                filterParameters.getWard(),
                filterParameters.getRoomType(),
                filterParameters.getMinPrice(),
                filterParameters.getMaxPrice(),
                filterParameters.getMinPeople(),
                filterParameters.getMaxPeople(),
                subscribedLandlordIds.toArray(new String[0]),
                filterParameters.isPivotSubscribed(),
                filterParameters.getTimestamp(),
                filterParameters.getPivotId(),
                filterParameters.getLimit(),
                filterParameters.getTagIds().toArray(new String[0])
        );
        return rooms.stream().map(this::mapFromDaoToResponse).collect(Collectors.toList());
    }

    @Override
    public List<RoomResponse> getSubscribedRoomsNearby(double latitude, double longitude,
                                                       double radiusKm) {
        List<String> landlordIds = subscriptionService.getLandlordsWithActiveSubscriptions();
        List<Room> rooms = roomRepository.findRoomsByLandlordIdsWithinRadius(landlordIds, latitude, longitude,
                radiusKm);
        return rooms.stream().map(this::mapToRoomResponse).collect(Collectors.toList());
    }

    @Override
    public List<RoomResponse> getSubscribedRoomsByLocation(String city, String district, String ward) {
        List<String> landlordIds = subscriptionService.getLandlordsWithActiveSubscriptions();
        List<Room> rooms = roomRepository.findRoomsByLandlordIdsAndLocation(landlordIds, city, district, ward);
        return rooms.stream().map(this::mapToRoomResponse).collect(Collectors.toList());
    }

    @Override
    public Boolean createRoom(CreateRoomRequest createRoomRequest, String landlordId) {
        User landlord = userService.getUserEntity(landlordId);
        List<Tag> tags = tagService.getTagsByIdIn(createRoomRequest.getTagIds());
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
                .rentalDeposit(createRoomRequest.getDeposit())
                .tags(new HashSet<>(tags))
                .squareMeters(createRoomRequest.getSquareMeters())
                .build();
        roomRepository.save(room);
        return true;
    }

    @Override
    public RoomResponse updateRoom(String roomId, UpdateRoomRequest updateRoomRequest) {
        Room room = getRoomEntityById(roomId);
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
        room.setRentalDeposit(updateRoomRequest.getDeposit());
        Room updatedRoom = roomRepository.save(room);
        return mapToRoomResponse(updatedRoom);
    }

    @Override
    public void deleteRoom(String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("Room", "id", roomId)
        );
        room.setStatus(RoomStatus.DELETED);
        roomRepository.save(room);
    }

    @Override
    public BigDecimal getAveragePriceAroundRoom(String roomId, Double radius) {
        Room room = getRoomEntityById(roomId);
        List<Room> aroundRoom = roomRepository.findRoomAround(
                room.getLongitude(),
                room.getLatitude(),
                radius);
        Set<Tag> roomTag = room.getTags();
        BigDecimal totalWeightedPrice = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (Room loopRoom : aroundRoom) {
            double similarity = calculateTagSimilarity(roomTag, loopRoom.getTags());
            // Minimum weight in case no similarity, so this weighted is strong location base
            // The last part of weight is base on tags similarity
            double weight = AppConstants.MIN_WEIGHT + ((1 - AppConstants.MIN_WEIGHT) * similarity);
            BigDecimal roomWeight = BigDecimal.valueOf(weight);
            BigDecimal weightedPrice = loopRoom.getPrice().multiply(roomWeight);
            totalWeightedPrice = totalWeightedPrice.add(weightedPrice);
            totalWeight = totalWeight.add(roomWeight);
        }
        return totalWeightedPrice.divide(totalWeight, 2, RoundingMode.HALF_UP);
    }

    @Override
    public void updateRoomStatusByLandlordId(String landlordId, RoomStatus status) {

    }

    private FilterParameters normalizeRoomFilterRequest(RoomFilterRequest request) {
        List<String> tagIds = request.getTagIds() != null ? request.getTagIds() : Collections.emptyList();
        return FilterParameters.builder()
                .city(normalizeString(request.getCity()))
                .district(normalizeString(request.getDistrict()))
                .ward(normalizeString(request.getWard()))
                .roomType(parseRoomType(request.getType()))
                .minPrice(request.getMinPrice() != null ? BigDecimal.valueOf(request.getMinPrice()) : DEFAULT_MIN_PRICE)
                .maxPrice(request.getMaxPrice() != null ? BigDecimal.valueOf(request.getMaxPrice()) : DEFAULT_MAX_PRICE)
                .minPeople(request.getMinPeople() != null ? request.getMinPeople() : DEFAULT_MIN_PEOPLE)
                .maxPeople(request.getMaxPeople() != null ? request.getMaxPeople() : DEFAULT_MAX_PEOPLE)
                .pivotId(request.getPivotId() != null ? request.getPivotId() : "zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz")
                .limit(request.getLimit() != null ? request.getLimit() : DEFAULT_LIMIT)
                .timestamp(request.getTimestamp() != null ? LocalDateTime.parse(
                        request.getTimestamp()) : LocalDateTime.now().plusDays(1))
                .pivotSubscribed(request.isSubscribed())
                .tagIds(tagIds)
                .build();
    }

    private String normalizeString(String value) {
        return value != null ? value : "";
    }

    private String parseRoomType(String type) {
        if (type == null || type.isEmpty()) {
            return null;
        }
        try {
            return RoomType.valueOf(type).name();
        } catch (IllegalArgumentException e) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR, "Invalid room type");
        }
    }

    private String getNearbyAmenitiesString(Double latitude, Double longitude) {
        // TODO: Implement this method to get nearby amenities from latitude and longitude by calling Google Maps API
        return "";
    }

    private RoomResponse mapToRoomResponse(Room room) {
        return getRoomResponse(room);
    }

    private double calculateTagSimilarity(Set<Tag> tags1, Set<Tag> tags2) {
        if (tags1.isEmpty() && tags2.isEmpty())
            return 1;
        Set<Tag> intersect = new HashSet<>(tags1);
        intersect.retainAll(tags2);
        Set<Tag> union = new HashSet<>(tags1);
        union.addAll(tags2);
        return (double) intersect.size() / union.size();
    }

    private RoomResponse mapFromDaoToResponse(RoomDao roomDao) {
        return RoomResponse.builder()
                .id(roomDao.getId())
                .title(roomDao.getTitle())
                .description(roomDao.getDescription())
                .address(roomDao.getAddress())
                .status(roomDao.getStatus())
                .price(roomDao.getPrice())
                .latitude(roomDao.getLatitude())
                .longitude(roomDao.getLongitude())
                .city(roomDao.getCity())
                .district(roomDao.getDistrict())
                .ward(roomDao.getWard())
                .electricPrice(roomDao.getElectricPrice())
                .waterPrice(roomDao.getWaterPrice())
                .type(roomDao.getType())
                .nearbyAmenities(roomDao.getNearbyAmenities())
                .maxPeople(roomDao.getMaxPeople())
                .landlordId(roomDao.getLandlordId())
                .tags(roomRepository.findTagsById(roomDao.getId()))
                .deposit(roomDao.getDeposit())
                .createdAt(roomDao.getCreatedAt().toLocalDateTime())
                .updatedAt(roomDao.getUpdatedAt().toLocalDateTime())
                .squareMeters(roomDao.getSquareMeters())
                .isSubscribed(roomDao.isSubscribed())
                .build();
    }
}
