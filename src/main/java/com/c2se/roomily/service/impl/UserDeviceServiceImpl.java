package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.User;
import com.c2se.roomily.entity.UserDevice;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.payload.request.UserDeviceRegisterRequest;
import com.c2se.roomily.payload.response.UserDeviceResponse;
import com.c2se.roomily.repository.UserDeviceRepository;
import com.c2se.roomily.repository.UserRepository;
import com.c2se.roomily.service.UserDeviceService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDeviceServiceImpl implements UserDeviceService {
    private final UserRepository userRepository;
    private final UserDeviceRepository userDeviceRepository;
    @Override
    public void registerDevice(UserDeviceRegisterRequest request) {
        User user = userRepository.findById(request.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", request.getUserId())
        );
        UserDevice userDevice = userDeviceRepository.findByFcmTokenAndUserId(request.getFcmToken(),
                                                                                       request.getUserId());
        if (userDevice != null){
            userDevice.setLastLoginAt(LocalDateTime.now());
            userDeviceRepository.save(userDevice);
            return;
        }
        userDevice = UserDevice.builder()
                .user(user)
                .deviceType(request.getDeviceType())
                .fcmToken(request.getFcmToken())
                .lastLoginAt(LocalDateTime.now())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        userDeviceRepository.save(userDevice);
    }

    @Override
    public List<String> getActiveUserToken(String userId) {
        return userDeviceRepository.findByUserIdAndIsActiveTrue(userId).stream().map(UserDevice::getFcmToken).toList();
    }

    @Override
    public List<UserDeviceResponse> getDeviceByUserId(String userId) {
        List<UserDevice> userDevices = userDeviceRepository.findByUserId(userId);
        if (userDevices != null){
            return userDevices.stream().map((userDevice -> UserDeviceResponse.builder()
                   .id(userDevice.getId())
                   .deviceType(userDevice.getDeviceType().name())
                   .fcmToken(userDevice.getFcmToken())
                   .isActive(userDevice.getIsActive())
                   .lastLoginAt(userDevice.getLastLoginAt())
                   .build())).toList();
        }
        return null;
    }

    @Override
    public boolean isDeviceActive(String fcmToken) {
        return userDeviceRepository.existsByFcmTokenAndIsActiveTrue(fcmToken);
    }

    @Override
    public boolean deactivateDevice(String fcmToken) {
        UserDevice userDevice = userDeviceRepository.findByFcmToken(fcmToken).orElseThrow(
                ()-> new ResourceNotFoundException("UserDevice", "fcmToken", fcmToken)
        );
        userDevice.setIsActive(false);
        userDeviceRepository.save(userDevice);
        return true;
    }

    @Override
    @Transactional
    public void deleteDevice(String fcmToken) {
        UserDevice userDevice = userDeviceRepository.findByFcmToken(fcmToken).orElseThrow(
                ()-> new ResourceNotFoundException("UserDevice", "fcmToken", fcmToken)
        );
        userDeviceRepository.delete(userDevice);
    }
}
