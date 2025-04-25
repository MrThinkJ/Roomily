package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.UserDeviceRegisterRequest;
import com.c2se.roomily.payload.response.UserDeviceResponse;

import java.util.List;

public interface UserDeviceService {
    void registerDevice(UserDeviceRegisterRequest userDeviceRegisterRequest);
    List<String> getActiveUserToken(String userId);
    List<UserDeviceResponse> getDeviceByUserId(String userId);
    boolean isDeviceActive(String fcmToken);
    boolean deactivateDevice(String fcmToken);
    void deleteDevice(String fcmToken);
}
