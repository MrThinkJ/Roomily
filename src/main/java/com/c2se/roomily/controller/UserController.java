package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.UpdateUserRequest;
import com.c2se.roomily.payload.response.PageUserResponse;
import com.c2se.roomily.payload.response.UserResponse;
import com.c2se.roomily.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController extends BaseController {
    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserByUserId(userId));
    }

    @PutMapping
    public ResponseEntity<Void> updateUser(@ModelAttribute UpdateUserRequest request) {
        String userId = this.getUserInfo().getId();
        userService.updateUser(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<PageUserResponse> getUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(userService.getUsers(page, size, sortBy, sortDir));
    }

    @GetMapping("/status")
    public ResponseEntity<PageUserResponse> getUsersByStatus(
            @RequestParam(value = "status", defaultValue = "ACTIVE") String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(userService.getUsersByStatus(status, page, size, sortBy, sortDir));
    }

    @GetMapping("/verified")
    public ResponseEntity<PageUserResponse> getUsersByIsVerified(
            @RequestParam(value = "isVerified", defaultValue = "true") boolean isVerified,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(userService.getUsersByIsVerified(isVerified, page, size, sortBy, sortDir));
    }

    @GetMapping("/rating")
    public ResponseEntity<PageUserResponse> getUsersByRatingInRange(
            @RequestParam(value = "minRating", defaultValue = "0") double minRating,
            @RequestParam(value = "maxRating", defaultValue = "5") double maxRating,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(
                userService.getUsersByRatingInRange(minRating, maxRating, page, size, sortBy, sortDir));
    }
}
