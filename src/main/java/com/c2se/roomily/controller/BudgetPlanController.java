package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.UserPreferencesRequest;
import com.c2se.roomily.payload.response.RoomBudgetPlanDetailResponse;
import com.c2se.roomily.payload.response.SearchedRoomResponse;
import com.c2se.roomily.service.BudgetPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budget-plan")
@RequiredArgsConstructor
public class BudgetPlanController extends BaseController{
    private final BudgetPlanService budgetPlanService;

    @GetMapping("/is-user-preference-exists")
    public ResponseEntity<Boolean> isUserPreferenceExists() {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(budgetPlanService.isUserPreferenceExists(userId));
    }

    @PostMapping("/save-user-preference")
    public ResponseEntity<Void> saveUserPreference(@RequestBody UserPreferencesRequest request) {
        String userId = this.getUserInfo().getId();
        budgetPlanService.saveUserPreference(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-searched-room")
    public ResponseEntity<List<SearchedRoomResponse>> getSearchedRoom() {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(budgetPlanService.getSearchedRoomResponse(userId));
    }

    @GetMapping("/get-room-budget-plan-detail/{roomId}")
    public ResponseEntity<RoomBudgetPlanDetailResponse> getRoomBudgetPlanDetail(@PathVariable String roomId,
                                                                                @RequestParam(defaultValue = "1") Integer areaType) {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(budgetPlanService.getRoomBudgetPlanDetail(roomId, userId, areaType));
    }
}
