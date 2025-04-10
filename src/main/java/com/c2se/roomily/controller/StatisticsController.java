package com.c2se.roomily.controller;

import com.c2se.roomily.payload.response.LandlordStatisticsResponse;
import com.c2se.roomily.payload.response.TenantStatisticsResponse;
import com.c2se.roomily.security.CustomUserDetails;
import com.c2se.roomily.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController extends BaseController {

    private final StatisticsService statisticsService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyStatistics() {
        CustomUserDetails currentUser = this.getCurrentUser();
        if (currentUser.getAuthorities().stream().anyMatch(
                authority -> authority.getAuthority().equals("ROLE_LANDLORD"))) {
            return ResponseEntity.ok(getLandlordStatistics(currentUser.getId()));
        } else
            return ResponseEntity.ok(statisticsService.getTenantStatistics(currentUser.getId()));
        }

    @GetMapping("/tenant/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LANDLORD')")
    public ResponseEntity<TenantStatisticsResponse> getTenantStatistics(@PathVariable String userId) {;
        return ResponseEntity.ok(statisticsService.getTenantStatistics(userId));
    }

    @GetMapping("/landlord/{userId}")
    public ResponseEntity<LandlordStatisticsResponse> getLandlordStatistics(@PathVariable String userId) {
        return ResponseEntity.ok(statisticsService.getLandlordStatistics(userId));
    }
}