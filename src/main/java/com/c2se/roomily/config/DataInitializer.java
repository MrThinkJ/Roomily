package com.c2se.roomily.config;

import com.c2se.roomily.entity.Role;
import com.c2se.roomily.entity.Tag;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.TagCategory;
import com.c2se.roomily.enums.UserStatus;
import com.c2se.roomily.payload.internal.TagData;
import com.c2se.roomily.repository.RoleRepository;
import com.c2se.roomily.repository.TagRepository;
import com.c2se.roomily.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Configuration
@Slf4j
public class DataInitializer {

    @Bean
    CommandLineRunner initTags(TagRepository tagRepository) {
        List<TagData> initialTags = Arrays.asList(
                // --- IN_ROOM_FEATURE ---
                new TagData("AIR_CONDITIONING", TagCategory.IN_ROOM_FEATURE, "Điều hòa"),
                new TagData("WASHING_MACHINE", TagCategory.IN_ROOM_FEATURE, "Máy giặt"),
                new TagData("BALCONY", TagCategory.IN_ROOM_FEATURE, "Ban công"),
                new TagData("FRIDGE", TagCategory.IN_ROOM_FEATURE, "Tủ lạnh"),
                new TagData("KITCHEN", TagCategory.IN_ROOM_FEATURE, "Có bếp"),
                new TagData("PRIVATE_BATHROOM", TagCategory.IN_ROOM_FEATURE, "WC riêng"),
                new TagData("WATER_HEATER", TagCategory.IN_ROOM_FEATURE, "Bình nóng lạnh"),
                new TagData("MICROWAVE", TagCategory.IN_ROOM_FEATURE, "Lò vi sóng"),
                new TagData("TV", TagCategory.IN_ROOM_FEATURE, "Tivi"),
                new TagData("WINDOW", TagCategory.IN_ROOM_FEATURE, "Cửa sổ"),
                new TagData("LARGE_WINDOW", TagCategory.IN_ROOM_FEATURE, "View đẹp"),
                new TagData("MEZZANINE", TagCategory.IN_ROOM_FEATURE, "Gác lửng / Gác xếp"),
                new TagData("FURNISHED_BASIC", TagCategory.IN_ROOM_FEATURE, "Nội thất cơ bản"),
                new TagData("FURNISHED_FULL", TagCategory.IN_ROOM_FEATURE, "Đầy đủ nội thất"),
                new TagData("UNFURNISHED", TagCategory.IN_ROOM_FEATURE, "Không có nội thất"),
                new TagData("AIRY", TagCategory.IN_ROOM_FEATURE, "Thoáng mát"),
                new TagData("NATURAL_LIGHT", TagCategory.IN_ROOM_FEATURE, "Nhiều ánh sáng"),

                // --- NEARBY_POI ---
                new TagData("GYM_NEARBY", TagCategory.NEARBY_POI, "Gần phòng Gym"),
                new TagData("MARKET_NEARBY", TagCategory.NEARBY_POI, "Gần chợ"),
                new TagData("SUPERMARKET_NEARBY", TagCategory.NEARBY_POI, "Gần siêu thị"),
                new TagData("CONVENIENCE_STORE_NEARBY", TagCategory.NEARBY_POI, "Gần cửa hàng tiện lợi"),
                new TagData("PARK_NEARBY", TagCategory.NEARBY_POI, "Gần công viên"),
                new TagData("SCHOOL_NEARBY", TagCategory.NEARBY_POI, "Gần trường học"),
                new TagData("UNIVERSITY_NEARBY", TagCategory.NEARBY_POI, "Gần trường Đại học"),
                new TagData("HOSPITAL_NEARBY", TagCategory.NEARBY_POI, "Gần bệnh viện"),
                new TagData("BUS_STOP_NEARBY", TagCategory.NEARBY_POI, "Gần bến xe buýt"),
                new TagData("RESTAURANT_NEARBY", TagCategory.NEARBY_POI, "Gần nhà hàng"),
                new TagData("CAFE_NEARBY", TagCategory.NEARBY_POI, "Gần quán cà phê"),
                new TagData("NEAR_BEACH", TagCategory.NEARBY_POI, "Gần biển"),
                new TagData("NEAR_DOWNTOWN", TagCategory.NEARBY_POI, "Gần trung tâm thành phố"),

                // --- POLICY ---
                new TagData("PET_ALLOWED", TagCategory.POLICY, "Cho phép nuôi thú cưng"),
                new TagData("WIFI_INCLUDED", TagCategory.POLICY, "Bao gồm Wifi"),
                new TagData("NO_CURFEW", TagCategory.POLICY, "Giờ giấc tự do"),
                new TagData("CURFEW_SPECIFIC", TagCategory.POLICY, "Có giới nghiêm (hỏi chủ nhà)"),
                new TagData("NO_SMOKING", TagCategory.POLICY, "Không hút thuốc"),
                new TagData("SEPARATE_ENTRANCE", TagCategory.POLICY, "Lối đi riêng"),
                new TagData("NOT_WITH_LANDLORD", TagCategory.POLICY, "Không chung chủ"),

                // --- BUILDING_FEATURE ---
                new TagData("ELEVATOR_AVAILABLE", TagCategory.BUILDING_FEATURE, "Có thang máy"),
                new TagData("PARKING_MOTORBIKE", TagCategory.BUILDING_FEATURE, "Có chỗ để xe máy"),
                new TagData("PARKING_CAR", TagCategory.BUILDING_FEATURE, "Có chỗ đậu ô tô"),
                new TagData("COVERED_PARKING", TagCategory.BUILDING_FEATURE, "Chỗ để xe có mái che"),
                new TagData("SECURITY_GUARD", TagCategory.BUILDING_FEATURE, "Có bảo vệ"),
                new TagData("CCTV", TagCategory.BUILDING_FEATURE, "Có Camera an ninh"),
                new TagData("QUIET_AREA", TagCategory.BUILDING_FEATURE, "Khu vực yên tĩnh"),
                new TagData("GOOD_SECURITY", TagCategory.BUILDING_FEATURE, "Khu vực an ninh tốt"));

        return args -> {
            initialTags.forEach(tagData -> {
                if (tagRepository.findByName(tagData.getName()).isEmpty()) {
                    Tag newTag = Tag.builder()
                            .name(tagData.getName())
                            .category(tagData.getCategory())
                            .displayName(tagData.getDisplayName())
                            .build();
                    tagRepository.save(newTag);
                    log.info("Tag '{}' (Display: '{}', Category: {}) has been created as default",
                             tagData.getName(), tagData.getDisplayName(), tagData.getCategory());
                } else {
                    log.info("Tag '{}' already exists.", tagData.getName());
                }
            });
            log.info("Finished initializing default tags.");
        };
    }

    @Bean
    ApplicationRunner initData(RoleRepository roleRepository,
                               UserRepository userRepository) {
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_LANDLORD", "ROLE_USER");
        return args -> {
            roles.forEach(role -> {
                if (roleRepository.findByName(role) == null) {
                    Role newRole = Role.builder()
                            .name(role)
                            .build();
                    roleRepository.save(newRole);
                    log.info("Role {} has been created as default", role);
                }
            });
            User admin = userRepository.findByUsername("admin");
            if (admin == null) {
                User newAdmin = User.builder()
                        .username("admin")
                        .password("admin")
                        .email("admin@gmail.com")
                        .isVerified(true)
                        .status(UserStatus.ACTIVE)
                        .roles(new HashSet<>(List.of(roleRepository.findByName("ROLE_ADMIN"))))
                        .build();
                userRepository.save(newAdmin);
            }
        };
    }
}
