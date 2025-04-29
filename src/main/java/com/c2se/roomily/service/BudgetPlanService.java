package com.c2se.roomily.service;

import com.c2se.roomily.payload.request.UserPreferencesRequest;
import com.c2se.roomily.payload.response.RoomBudgetPlanDetailResponse;
import com.c2se.roomily.payload.response.SearchedRoomResponse;

import java.util.List;

public interface BudgetPlanService {
    boolean isUserPreferenceExists(String userId);
    void saveUserPreference(String userId, UserPreferencesRequest request);
    List<SearchedRoomResponse> getSearchedRoomResponse(String userId);
    RoomBudgetPlanDetailResponse getRoomBudgetPlanDetail(String roomId, String userId, Integer areaType);
}
