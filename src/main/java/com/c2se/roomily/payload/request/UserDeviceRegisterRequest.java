package com.c2se.roomily.payload.request;

import com.c2se.roomily.enums.DeviceType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDeviceRegisterRequest {
    @NotNull(message = "User id is required")
    private String userId;
    @NotNull(message = "Fcm token is required")
    private String fcmToken;
    @NotNull(message = "Device type is required")
    private DeviceType deviceType;
}
