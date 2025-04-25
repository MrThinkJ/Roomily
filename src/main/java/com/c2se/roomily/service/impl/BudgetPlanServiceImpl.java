package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.Room;
import com.c2se.roomily.entity.Tag;
import com.c2se.roomily.entity.UserPreference;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.UserPreferencesRequest;
import com.c2se.roomily.payload.response.RoomBudgetPlanDetailResponse;
import com.c2se.roomily.payload.response.RoomResponse;
import com.c2se.roomily.payload.response.SearchedRoomResponse;
import com.c2se.roomily.repository.FindPartnerPostRepository;
import com.c2se.roomily.repository.RoomRepository;
import com.c2se.roomily.repository.UserPreferenceRepository;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.service.BudgetPlanService;
import com.c2se.roomily.service.RoomImageService;
import com.c2se.roomily.service.TagService;
import com.c2se.roomily.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetPlanServiceImpl implements BudgetPlanService {
    private final UserPreferenceRepository userPreferenceRepository;
    private final TagService tagService;
    private final UserService userService;
    private final RoomRepository roomRepository;
    private final FindPartnerPostRepository findPartnerPostRepository;
    private final RoomImageService roomImageService;

    private final int AVERAGE_WATER_MONTHLY_USAGE = 6; // in m3
    private final int AVERAGE_ELECTRICITY_MONTHLY_USAGE = 300; // in kWh
    private final int AVERAGE_WIFI_COST = 200000; // in VND

    @Override
    public boolean isUserPreferenceExists(String userId) {
        return userPreferenceRepository.existsByUserId(userId);
    }

    @Override
    public void saveUserPreference(String userId, UserPreferencesRequest request) {
        UserPreference userPreference = userPreferenceRepository.findByUserId(userId)
                .orElse(new UserPreference());
        userPreference.setCity(request.getCity());
        userPreference.setDistrict(request.getDistrict());
        userPreference.setWard(request.getWard());
        userPreference.setRoomType(request.getRoomType());
        userPreference.setMaxBudget(request.getMaxBudget());
        userPreference.setMonthlySalary(request.getMonthlySalary());
        userPreference.setMustHaveTags(request.getMustHaveTagIds().stream().map(
                tagService::getTagById).collect(Collectors.toSet()));
        userPreference.setNiceToHaveTags(request.getNiceToHaveTagIds().stream().map(
                tagService::getTagById).collect(Collectors.toSet()));
        userPreference.setUserId(userId);
        userPreferenceRepository.save(userPreference);
    }

    @Override
    public List<SearchedRoomResponse> getSearchedRoomResponse(String userId) {
        UserPreference userPreference = userPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Preference", "userId", userId));
        List<Room> preferenceRooms = roomRepository.findRoomByUserPreference(
            userPreference.getCity(),
            userPreference.getDistrict(),
            userPreference.getWard(),
            userPreference.getRoomType().name(),
            userPreference.getMaxBudget(),
            userPreference.getMustHaveTags().stream().map(Tag::getId).collect(Collectors.toSet()),
            userPreference.getMustHaveTags().size()
        );
        return preferenceRooms.stream()
                .map(room -> {
                    Set<Tag> roomTags = room.getTags();
                    Set<Tag> niceTags = userPreference.getNiceToHaveTags();
                    Double tagSimilarity = calculateTagSimilarity(roomTags, niceTags);
                    return SearchedRoomResponse.builder()
                            .roomId(room.getId())
                            .roomTitle(room.getTitle())
                            .roomDescription(room.getDescription())
                            .roomAddress(room.getAddress())
                            .squareMeters(room.getSquareMeters())
                            .roomType(room.getType().name())
                            .city(room.getCity())
                            .district(room.getDistrict())
                            .ward(room.getWard())
                            .latitude(room.getLatitude())
                            .longitude(room.getLongitude())
                            .numberOfTagsMatched((int) (tagSimilarity * roomTags.size()) +
                                                         userPreference.getMustHaveTags().size())
                            .numberOfTags(roomTags.size())
                            .imageUrl(roomImageService.getRoomImageUrlsByRoomId(room.getId()).get(0))
                            .tagSimilarity(tagSimilarity)
                            .build();
                }).sorted((o1, o2) -> {
                    if (o1.getTagSimilarity() > o2.getTagSimilarity()) {
                        return -1;
                    } else if (o1.getTagSimilarity() < o2.getTagSimilarity()) {
                        return 1;
                    }
                    return 0;
                }).collect(Collectors.toList());
    }

    @Override
    public RoomBudgetPlanDetailResponse getRoomBudgetPlanDetail(String roomId, String userId, Integer areaType) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));
        String city;
        String district = null;
        String ward = null;
        if (areaType == 1){
            city = room.getCity();
            district = room.getDistrict();
            ward = room.getWard();
        } else if (areaType == 2){
            city = room.getCity();
            district = room.getDistrict();
        } else if (areaType == 3){
            city = room.getCity();
        } else {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "Invalid area type", "Area type must be 1, 2 or 3");
        }
        List<Room> baseLineRooms = roomRepository.findRoomByUserPreference(
                city,
                district,
                ward,
                room.getType().name(),
                room.getPrice().add(room.getRentalDeposit()),
                Set.of(),
                0
        );
        baseLineRooms.sort(Comparator.comparing(Room::getPrice));
        BigDecimal baseLineMinRentalCost = baseLineRooms.get(0).getPrice();
        BigDecimal baseLineMaxRentalCost = baseLineRooms.get(baseLineRooms.size() - 1).getPrice();
        BigDecimal baseLineMedianRentalCost = baseLineRooms.get(baseLineRooms.size() / 2).getPrice();
        UserPreference userPreference = userPreferenceRepository.findByUserId(userId).orElseThrow(
                () -> new ResourceNotFoundException("User Preference", "userId", userId));
        RoomBudgetPlanDetailResponse response = new RoomBudgetPlanDetailResponse();
        response.setUpFrontCost(room.getPrice().add(room.getRentalDeposit()));
        response.setEstimatedMonthlyElectricityUsage(AVERAGE_ELECTRICITY_MONTHLY_USAGE);
        response.setEstimatedMonthlyWaterUsage(AVERAGE_WATER_MONTHLY_USAGE);
        boolean isIncludeWifi = room.getTags().stream().anyMatch(
                tag -> tag.getName().equalsIgnoreCase("WIFI_INCLUDED"));
        response.setIncludeWifi(isIncludeWifi);
        response.setWifiCost(isIncludeWifi ? BigDecimal.valueOf(AVERAGE_WIFI_COST) : BigDecimal.ZERO);
        response.setHasUserMonthlySalary(userPreference.getMonthlySalary() != null);
        response.setMonthlySalary(userPreference.getMonthlySalary());
        response.setMaxBudget(userPreference.getMaxBudget());
        response.setBaseLineMinRentalCost(baseLineMinRentalCost);
        response.setBaseLineMaxRentalCost(baseLineMaxRentalCost);
        response.setBaseLineMedianRentalCost(baseLineMedianRentalCost);
        response.setAverageElectricityCost(roomRepository.getAverageElectricityCostInDistrict(
                room.getCity(), room.getDistrict(), room.getType()));
        response.setAverageWaterCost(roomRepository.getAverageWaterCostInDistrict(
                room.getCity(), room.getDistrict(), room.getType()));
        response.setRoomResponse(convertToRoomResponse(room));
        Set<Tag> roomTags = room.getTags();
        Set<Tag> mustHaveTags = userPreference.getMustHaveTags();
        Set<Tag> niceTags = userPreference.getNiceToHaveTags();
        Set<Tag> matchedTags = new HashSet<>(mustHaveTags);
        matchedTags.retainAll(roomTags);
        Set<Tag> unmatchedTags = new HashSet<>(roomTags);
        unmatchedTags.removeAll(matchedTags);
        unmatchedTags.retainAll(niceTags);
        response.setMatchedTags(matchedTags);
        response.setUnmatchedTags(unmatchedTags);
        return response;
    }

    private Double calculateTagSimilarity(Set<Tag> roomTags, Set<Tag> niceTags) {
        if (roomTags.isEmpty() || niceTags.isEmpty()) {
            return 0.0;
        }
        Set<Tag> intersect = new HashSet<>(niceTags);
        intersect.retainAll(roomTags);
        long matchedTagSize = intersect.size();
        return (double) matchedTagSize / (double) roomTags.size();
    }

    private RoomResponse convertToRoomResponse(Room room) {
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
