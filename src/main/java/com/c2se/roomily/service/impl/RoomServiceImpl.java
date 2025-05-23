package com.c2se.roomily.service.impl;

import com.c2se.roomily.config.RabbitMQConfig;
import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.Tag;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.*;
import com.c2se.roomily.event.pojo.CreateRoomEvent;
import com.c2se.roomily.event.pojo.RoomDeleteEvent;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.dao.RoomDao;
import com.c2se.roomily.payload.internal.FilterParameters;
import com.c2se.roomily.payload.internal.GooglePlacesResponseResult;
import com.c2se.roomily.payload.internal.GooglePlacesTag;
import com.c2se.roomily.payload.request.CreateRoomRequest;
import com.c2se.roomily.payload.request.RoomFilterRequest;
import com.c2se.roomily.payload.request.UpdateRoomRequest;
import com.c2se.roomily.payload.response.RoomResponse;
import com.c2se.roomily.repository.FindPartnerPostRepository;
import com.c2se.roomily.repository.RentedRoomRepository;
import com.c2se.roomily.repository.RoomRepository;
import com.c2se.roomily.security.CustomUserDetails;
import com.c2se.roomily.service.*;
import com.c2se.roomily.util.AppConstants;
import com.c2se.roomily.util.UtilFunction;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final RentedRoomRepository rentedRoomRepository;
    private final TagService tagService;
    private final UserService userService;
    private final ContractGenerationService contractGenerationService;
    private final EventService eventService;
    private final FindPartnerPostRepository findPartnerPostRepository;
    private final RabbitTemplate rabbitTemplate;
    private final WebClient webClient;
    private final BigDecimal DEFAULT_MIN_PRICE = BigDecimal.ZERO;
    private final BigDecimal DEFAULT_MAX_PRICE = BigDecimal.valueOf(1_000_000_000);
    private final int DEFAULT_MIN_PEOPLE = 0;
    private final int DEFAULT_MAX_PEOPLE = 100;
    private final int DEFAULT_LIMIT = 20;
    @Value("${google.map.api-key}")
    private String googleMapApiKey;
    private final long searchRadius = 2000;
    private final Map<String, List<String>> tagToGoogleTypes = Map.ofEntries(
            Map.entry("GYM_NEARBY", List.of("gym")),
            Map.entry("MARKET_NEARBY", List.of("market")),
            Map.entry("SUPERMARKET_NEARBY", List.of("supermarket")),
            Map.entry("CONVENIENCE_STORE_NEARBY", List.of("convenience_store")),
            Map.entry("PARK_NEARBY", List.of("park")),
            Map.entry("SCHOOL_NEARBY", List.of("school", "primary_school", "secondary_school")),
            Map.entry("UNIVERSITY_NEARBY", List.of("university")),
            Map.entry("HOSPITAL_NEARBY", List.of("hospital")),
            Map.entry("BUS_STOP_NEARBY", List.of("bus_station", "transit_station")),
            Map.entry("RESTAURANT_NEARBY", List.of("restaurant")),
            Map.entry("CAFE_NEARBY", List.of("cafe"))
    );

    @Override
    public Room getRoomEntityById(String roomId) {
        return roomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("Room", "Id", roomId)
        );
    }

    @Override
    public RoomResponse getRoomById(String roomId) {
        Room room = getRoomEntityById(roomId);
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"))) {
            Map<String, String> body = new HashMap<>();
            body.put("user_id", userDetails.getId());
            body.put("room_id", roomId);
            body.put("interaction_weight", String.valueOf(AppConstants.INTERACTION_WEIGHT_MAP.get("VIEW")));
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,
                                          RabbitMQConfig.EVENT_ROUTING_KEY,
                                          body);
        }
        return this.mapToRoomResponse(room);
    }

    @Override
    public Set<GooglePlacesTag> getRecommendedTagsByLocation(BigDecimal latitude, BigDecimal longitude) {
        latitude = latitude.setScale(10, RoundingMode.HALF_UP);
        longitude = longitude.setScale(10, RoundingMode.HALF_UP);
        return fetchNearbyAmenities(latitude.doubleValue(),
                                    longitude.doubleValue(),
                                    searchRadius);
    }

    @Override
    public void saveRoom(Room room) {
        roomRepository.save(room);
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
                filterParameters.isHasFindPartnerPost(),
                filterParameters.getTimestamp(),
                filterParameters.getPivotId(),
                filterParameters.getLimit(),
                filterParameters.getTagIds().toArray(new String[0])
        );
        return rooms.stream().map(this::mapFromDaoToResponse).collect(Collectors.toList());
    }

    @Override
    public String createRoom(CreateRoomRequest createRoomRequest, String landlordId) {
        User landlord = userService.getUserEntityById(landlordId);
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
                .electricityPrice(BigDecimal.valueOf(Double.parseDouble(createRoomRequest.getElectricPrice())))
                .waterPrice(BigDecimal.valueOf(Double.parseDouble(createRoomRequest.getWaterPrice())))
                .type(RoomType.valueOf(createRoomRequest.getType()))
                .maxPeople(createRoomRequest.getMaxPeople())
                .landlord(landlord)
                .rentalDeposit(BigDecimal.valueOf(Double.parseDouble(createRoomRequest.getDeposit())))
                .tags(new HashSet<>(tags))
                .squareMeters(createRoomRequest.getSquareMeters())
                .build();
        Room savedRoom = roomRepository.save(room);
        eventService.publishEvent(CreateRoomEvent.builder(this)
                                          .roomId(savedRoom.getId())
                                          .build());
        contractGenerationService.generateRoomContract(savedRoom);
        return savedRoom.getId();
    }

    @Override
    @Transactional
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
        room.setElectricityPrice(BigDecimal.valueOf(Double.parseDouble(updateRoomRequest.getElectricPrice())));
        room.setWaterPrice(BigDecimal.valueOf(Double.parseDouble(updateRoomRequest.getWaterPrice())));
        room.setType(RoomType.valueOf(updateRoomRequest.getType()));
        room.setMaxPeople(updateRoomRequest.getMaxPeople());
        room.setTags(updateRoomRequest.getTags().stream()
                             .map(tagService::getTagById)
                             .collect(Collectors.toSet()));
        room.setSquareMeters(updateRoomRequest.getSquareMeters());
        room.setRentalDeposit(BigDecimal.valueOf(Double.parseDouble(updateRoomRequest.getDeposit())));
        Room updatedRoom = roomRepository.save(room);
        eventService.publishEvent(CreateRoomEvent.builder(this)
                                          .roomId(updatedRoom.getId())
                                          .build());
        System.out.println("Handling CreateRoomEvent for roomId: " + updatedRoom.getId());
        return mapToRoomResponse(updatedRoom);
    }

    @Override
    public void deleteRoom(String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(
                () -> new ResourceNotFoundException("Room", "id", roomId)
        );
        RentedRoom rentedRoom = rentedRoomRepository.findActiveByRoomId(roomId, List.of(RentedRoomStatus.IN_USE,
                                                                                        RentedRoomStatus.DEBT,
                                                                                        RentedRoomStatus.DEPOSIT_NOT_PAID,
                                                                                        RentedRoomStatus.BILL_MISSING,
                                                                                        RentedRoomStatus.PENDING));
        if (rentedRoom != null) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "Cannot delete room because it is currently rented");
        }
        room.setStatus(RoomStatus.DELETED);
        roomRepository.save(room);
        Map<String, String> body = new HashMap<>();
        body.put("room_id", roomId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,
                                      RabbitMQConfig.ROOM_ROUTING_KEY,
                                      body);
        eventService.publishEvent(RoomDeleteEvent.builder(this)
                                          .roomId(roomId)
                                          .build());
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
        List<Room> rooms = roomRepository.findByLandlordId(landlordId);
        for (Room room : rooms) {
            room.setStatus(status);
        }
        roomRepository.saveAll(rooms);
    }

    @Override
    public void setRoomFindPartnerOnly(String roomId) {
        Room room = getRoomEntityById(roomId);
        room.setStatus(RoomStatus.FIND_PARTNER_ONLY);
        roomRepository.save(room);
    }

    private Set<GooglePlacesTag> fetchNearbyAmenities(Double latitude, Double longitude, long searchRadius) {
        Set<GooglePlacesTag> foundTags = ConcurrentHashMap.newKeySet();
        List<Mono<Void>> monos = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : tagToGoogleTypes.entrySet()) {
            String tagName = entry.getKey();
            List<String> googleTypes = entry.getValue();

            for (String googleType : googleTypes) {
                Mono<Void> mono = webClient.get()
                        .uri(buildGoogleApiUrl(latitude, longitude, searchRadius, googleType))
                        .retrieve()
                        .bodyToMono(GooglePlacesResponseResult.class)
                        .flatMap(response -> {
                            if (response != null && response.getStatus().equals("OK")) {
                                JsonNode results = response.getResults();
                                if (results != null && !results.isEmpty()) {
                                    foundTags.add(new GooglePlacesTag(
                                            tagName,
                                            UtilFunction.calculateDistance(
                                                    latitude,
                                                    longitude,
                                                    results.get(0).get("geometry").get("location").get("lat").asDouble(),
                                                    results.get(0).get("geometry").get("location").get("lng").asDouble()
                                            ),
                                            results.get(0).get("name").asText(),
                                            results.get(0).get("geometry").get("location").get("lat").asDouble(),
                                            results.get(0).get("geometry").get("location").get("lng").asDouble()
                                    ));
                                }
                            }
                            return Mono.<Void>empty();
                        })
                        .onErrorResume(e -> {
                            System.err.println("Error in API call for type " + googleType + ": " + e.getMessage());
                            return Mono.empty();
                        });
                monos.add(mono);
            }
        }
        try {
            Mono.when(monos).block();
        } catch (Exception e) {
            System.err.println("Error waiting for API calls to complete: " + e.getMessage());
        }

        addNonNearbySearchTags(foundTags, latitude, longitude);
        return foundTags;
    }

    private String buildGoogleApiUrl(double lat, double lon, long radius, String type) {
        return String.format(
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&type=%s&radius=%s&language=vi&key=%s",
                lat, lon, type, radius, googleMapApiKey);
    }

    private void addNonNearbySearchTags(Set<GooglePlacesTag> tags, double lat, double lon) {
        // Implement logic for NEAR_BEACH, NEAR_DOWNTOWN
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
                .hasFindPartnerPost(request.isHasFindPartnerPost())
                .pivotId(request.getPivotId() != null ? request.getPivotId() : "zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz")
                .limit(request.getLimit() != null ? request.getLimit() : DEFAULT_LIMIT)
                .timestamp(request.getTimestamp() != null ? LocalDateTime.parse(
                        request.getTimestamp()) : LocalDateTime.now().plusDays(1))
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
                .hasFindPartnerPost(roomDao.isHasFindPartnerPost())
                .build();
    }

     public RoomResponse getRoomResponse(Room room) {
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
                .electricPrice(room.getElectricityPrice())
                .waterPrice(room.getWaterPrice())
                .type(room.getType().name())
                .nearbyAmenities(room.getNearbyAmenities())
                .maxPeople(room.getMaxPeople())
                .landlordId(room.getLandlord().getId())
                .tags(room.getTags())
                .deposit(room.getRentalDeposit().toString())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .squareMeters(room.getSquareMeters())
                .hasFindPartnerPost(findPartnerPostRepository.hasActiveFindPartnerPostByRoomId(room.getId()))
                .build();
    }
}
