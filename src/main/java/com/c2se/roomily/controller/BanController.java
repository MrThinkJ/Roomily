package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.BanUserRequest;
import com.c2se.roomily.payload.response.BanHistoryResponse;
import com.c2se.roomily.service.BanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ban")
@RequiredArgsConstructor
public class BanController {
    private final BanService banService;

    @GetMapping("/active")
    public ResponseEntity<List<BanHistoryResponse>> getAllActiveBans() {
        return ResponseEntity.ok(banService.getAllActiveBans());
    }

    @GetMapping("/isBanned/{userId}")
    public ResponseEntity<Boolean> isUserBanned(@PathVariable String userId) {
        return ResponseEntity.ok(banService.isUserBanned(userId));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<BanHistoryResponse>> getUserBanHistory(@PathVariable String userId,
                                                                      @RequestParam(defaultValue = "0") Integer page,
                                                                      @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(banService.getUserBanHistory(userId, page, size));
    }

    @PostMapping("/ban")
    public ResponseEntity<Void> banUser(@RequestBody BanUserRequest banUserRequest) {
        banService.banUser(banUserRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unban")
    public ResponseEntity<Void> unbanUser(@RequestBody String userId) {
        banService.unbanUser(userId);
        return ResponseEntity.ok().build();
    }
}
