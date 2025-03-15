package com.c2se.roomily.service;

import com.c2se.roomily.entity.FindPartnerPost;
import com.c2se.roomily.payload.request.CreateFindPartnerPostRequest;
import com.c2se.roomily.payload.request.RentalRequest;
import com.c2se.roomily.payload.request.UpdateFindPartnerPostRequest;

public interface FindPartnerService {
    FindPartnerPost getFindPartnerPostEntity(String findPartnerPostId);

    void updateFindPartnerPostStatus(String findPartnerPostId, String status);

    void createFindPartnerPost(String userId, CreateFindPartnerPostRequest request);

    void deleteFindPartnerPostByRoomId(String roomId);

    RentalRequest requestToJoinFindPartnerPost(String userId, String findPartnerPostId, String chatRoomId);

    void cancelRequestToJoinFindPartnerPost(String userId, String chatRoomId);

    void acceptRequestToJoinFindPartnerPost(String userId, String chatRoomId);

    void rejectRequestToJoinFindPartnerPost(String userId, String chatRoomId);

    void addParticipantToFindPartnerPost(String userId, String findPartnerPostId, String privateId);

    void removeParticipantFromFindPartnerPost(String userId, String findPartnerPostId, String participantId);

    void exitFindPartnerPost(String userId, String findPartnerPostId);

    void deleteFindPartnerPost(String userId, String findPartnerPostId);

    void exitWhenChatRoomIsFull(String userId, String findPartnerPostId);

    void updateFindPartnerPost(String userId, String findPartnerPostId,
                               UpdateFindPartnerPostRequest updateFindPartnerPostRequest);
}
