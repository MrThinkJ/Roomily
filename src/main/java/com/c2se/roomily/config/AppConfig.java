package com.c2se.roomily.config;

import com.c2se.roomily.entity.Role;
import com.c2se.roomily.entity.Tag;
import com.c2se.roomily.entity.User;
import com.c2se.roomily.enums.UserStatus;
import com.c2se.roomily.repository.RoleRepository;
import com.c2se.roomily.repository.TagRepository;
import com.c2se.roomily.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.List;

@Configuration
@Slf4j
public class AppConfig {
    @Bean
    public ApplicationRunner applicationRunner(RoleRepository roleRepository,
                                               UserRepository userRepository,
                                               TagRepository tagRepository) {
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_LANDLORD", "ROLE_USER");
        List<String> tags = List.of("Air Conditioning",
                                    "Balcony",
                                    "Bed",
                                    "Fridge",
                                    "Internet",
                                    "Kitchen",
                                    "Laundry",
                                    "Microwave",
                                    "Parking",
                                    "TV",
                                    "Water Heater");
        return args -> {
            tags.forEach(tag -> {
                if (tagRepository.findByName(tag) == null) {
                    tagRepository.save(Tag.builder()
                                               .name(tag)
                                               .build());
                    log.info("Tag {} has been created as default", tag);
                }
            });
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
