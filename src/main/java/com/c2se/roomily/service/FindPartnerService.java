package com.c2se.roomily.service;

import com.c2se.roomily.entity.FindPartnerPost;
import com.c2se.roomily.payload.request.CreateFindPartnerPostRequest;
import com.c2se.roomily.payload.request.RentalRequest;
import com.c2se.roomily.payload.request.UpdateFindPartnerPostRequest;
import com.c2se.roomily.payload.response.FindPartnerPostResponse;

import java.util.List;

public interface FindPartnerService {
    FindPartnerPost getFindPartnerPostEntity(String findPartnerPostId);
    FindPartnerPostResponse getFindPartnerPostResponse(String findPartnerPostId);
    FindPartnerPost getAdditionalTenantFindPartnerPostEntityByRoomId(String roomId);

    List<FindPartnerPostResponse> getActiveFindPartnerPostsByRoomId(String roomId);

    List<FindPartnerPostResponse> getActiveFindPartnerPostsByUserId(String userId);

    Boolean isUserInActiveFindPartnerPostOfRoom(String userId, String roomId);

    void updateFindPartnerPostStatus(String findPartnerPostId, String status);

    void createFindPartnerPost(String userId, CreateFindPartnerPostRequest request);

    void deleteActiveFindPartnerPostByRoomId(String roomId);

    RentalRequest requestToJoinFindPartnerPost(String userId, String findPartnerPostId, String chatRoomId);

    void cancelRequestToJoinFindPartnerPost(String userId, String chatRoomId);

    void acceptRequestToJoinFindPartnerPost(String userId, String chatRoomId);

    void rejectRequestToJoinFindPartnerPost(String userId, String chatRoomId);

    void addParticipantToFindPartnerPost(String userId, String findPartnerPostId, String privateId);

    void removeParticipantFromFindPartnerPost(String userId, String findPartnerPostId, String participantId);

    void exitFindPartnerPost(String userId, String findPartnerPostId);

    void deleteFindPartnerPost(String userId, String findPartnerPostId);

    void updateFindPartnerPost(String userId, String findPartnerPostId,
                               UpdateFindPartnerPostRequest updateFindPartnerPostRequest);
}
