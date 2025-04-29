package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.UserDeviceRegisterRequest;
import com.c2se.roomily.payload.response.UserDeviceResponse;
import com.c2se.roomily.service.UserDeviceService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-devices")
@RequiredArgsConstructor
public class UserDeviceController {
    private final UserDeviceService userDeviceService;
    @GetMapping("/{userId}")
    public ResponseEntity<List<UserDeviceResponse>> getDevicesByUserId(@PathVariable String userId){
        return ResponseEntity.ok(userDeviceService.getDeviceByUserId(userId));
    }

    @PostMapping("/register")
    public ResponseEntity<Boolean> registerDevice(@RequestBody @Valid UserDeviceRegisterRequest request){
        userDeviceService.registerDevice(request);
        return ResponseEntity.ok(true);
    }

    @PatchMapping("/deactivate")
    public ResponseEntity<Boolean> deactivateDevice(@RequestParam String fcmToken){
        return ResponseEntity.ok(userDeviceService.deactivateDevice(fcmToken));
    }
}
