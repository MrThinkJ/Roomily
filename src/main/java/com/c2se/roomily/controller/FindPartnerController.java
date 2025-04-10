package com.c2se.roomily.controller;

import com.c2se.roomily.entity.FindPartnerPost;
import com.c2se.roomily.payload.request.CreateFindPartnerPostRequest;
import com.c2se.roomily.payload.request.RentalRequest;
import com.c2se.roomily.payload.request.RequestJoinFindPartnerPostRequest;
import com.c2se.roomily.payload.request.UpdateFindPartnerPostRequest;
import com.c2se.roomily.payload.response.FindPartnerPostResponse;
import com.c2se.roomily.service.FindPartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/find-partner")
@RequiredArgsConstructor
public class FindPartnerController extends BaseController {
    private final FindPartnerService findPartnerService;

    @GetMapping("/active/room/{roomId}")
    public ResponseEntity<List<FindPartnerPostResponse>> getActiveFindPartnerPosts(@PathVariable String roomId) {
        return ResponseEntity.ok(findPartnerService.getActiveFindPartnerPostsByRoomId(roomId));
    }

    @GetMapping("/active/user")
    public ResponseEntity<List<FindPartnerPostResponse>> getActiveFindPartnerPostsByUserId() {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(findPartnerService.getActiveFindPartnerPostsByUserId(userId));
    }

    @GetMapping("/active/{userId}/{roomId}")
    public ResponseEntity<Boolean> isUserInActiveFindPartnerPostOfRoom(@PathVariable String userId,
                                                                     @PathVariable String roomId) {
        return ResponseEntity.ok(findPartnerService.isUserInActiveFindPartnerPostOfRoom(userId, roomId));
    }

    @PostMapping
    public ResponseEntity<Void> createFindPartnerPost(@RequestBody CreateFindPartnerPostRequest request) {
        String userId = this.getUserInfo().getId();
        findPartnerService.createFindPartnerPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/request")
    public ResponseEntity<RentalRequest> requestToJoinFindPartnerPost(
            @RequestBody RequestJoinFindPartnerPostRequest request) {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(findPartnerService.requestToJoinFindPartnerPost(userId, request.getFindPartnerPostId(),
                                                                                 request.getChatRoomId()));
    }

    @DeleteMapping("/request/cancel/{chatRoomId}")
    public ResponseEntity<Void> cancelRequestToJoinFindPartnerPost(@PathVariable String chatRoomId) {
        String userId = this.getUserInfo().getId();
        findPartnerService.cancelRequestToJoinFindPartnerPost(userId, chatRoomId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accept-request/{chatRoomId}")
    public ResponseEntity<Void> acceptRequestToJoinFindPartnerPost(@PathVariable String chatRoomId) {
        String userId = this.getUserInfo().getId();
        findPartnerService.acceptRequestToJoinFindPartnerPost(userId, chatRoomId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reject-request/{chatRoomId}")
    public ResponseEntity<Void> rejectRequestToJoinFindPartnerPost(@PathVariable String chatRoomId) {
        String userId = this.getUserInfo().getId();
        findPartnerService.rejectRequestToJoinFindPartnerPost(userId, chatRoomId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{findPartnerPostId}/add-participant")
    public ResponseEntity<Void> addParticipantToFindPartnerPost(@PathVariable String findPartnerPostId,
                                                                @RequestParam String privateId) {
        String userId = this.getUserInfo().getId();
        findPartnerService.addParticipantToFindPartnerPost(userId, findPartnerPostId, privateId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{findPartnerPostId}/remove-participant")
    public ResponseEntity<Void> removeParticipantFromFindPartnerPost(@PathVariable String findPartnerPostId,
                                                                     @RequestParam String participantId) {
        String userId = this.getUserInfo().getId();
        findPartnerService.removeParticipantFromFindPartnerPost(userId, findPartnerPostId, participantId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{findPartnerPostId}/exit")
    public ResponseEntity<Void> exitFindPartnerPost(@PathVariable String findPartnerPostId) {
        String userId = this.getUserInfo().getId();
        findPartnerService.exitFindPartnerPost(userId, findPartnerPostId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{findPartnerPostId}")
    public ResponseEntity<Void> deleteFindPartnerPost(@PathVariable String findPartnerPostId) {
        String userId = this.getUserInfo().getId();
        findPartnerService.deleteFindPartnerPost(userId, findPartnerPostId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{findPartnerPostId}")
    public ResponseEntity<Void> updateFindPartnerPost(@PathVariable String findPartnerPostId,
                                                      @RequestBody UpdateFindPartnerPostRequest request) {
        String userId = this.getUserInfo().getId();
        findPartnerService.updateFindPartnerPost(userId, findPartnerPostId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{findPartnerPostId}")
    public ResponseEntity<FindPartnerPostResponse> getFindPartnerPost(@PathVariable String findPartnerPostId) {
        FindPartnerPostResponse findPartnerPost = findPartnerService.getFindPartnerPostResponse(findPartnerPostId);
        return ResponseEntity.ok(findPartnerPost);
    }
}
