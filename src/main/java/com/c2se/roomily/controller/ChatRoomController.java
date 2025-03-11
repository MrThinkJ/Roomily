package com.c2se.roomily.controller;

import com.c2se.roomily.payload.response.ChatRoomResponse;
import com.c2se.roomily.payload.response.ConversationResponse;
import com.c2se.roomily.service.ChatRoomService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/chat-rooms")
public class ChatRoomController extends BaseController {
    ChatRoomService chatRoomService;
    @PostMapping("/test")
    public void testNotifyChatRoom() {
        chatRoomService.testNotifyChatRoom();
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomResponse> getChatRoomInfo(@PathVariable String roomId) {
        return ResponseEntity.ok(chatRoomService.getChatRoomInfo(roomId));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ConversationResponse>> getChatRoomsByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(chatRoomService.getChatRoomsByUserId(userId));
    }

    @GetMapping("/my-chats")
    public ResponseEntity<List<ConversationResponse>> getMyChats() {
        String userId = this.getUserInfo().getId();
        return ResponseEntity.ok(chatRoomService.getChatRoomsByUserId(userId));
    }

    @GetMapping("/{roomId}/users")
    public ResponseEntity<List<String>> getChatRoomUserIds(@PathVariable String roomId) {
        return ResponseEntity.ok(chatRoomService.getChatRoomUserIds(roomId));
    }

    @PostMapping("/direct/{userId}")
    public ResponseEntity<ChatRoomResponse> getOrCreateDirectChatRoom(@PathVariable String userId,
                                                                      @RequestParam(required = false) String findPartnerPostId) {
        String currentUserId = this.getUserInfo().getId();
        ChatRoomResponse chatRoomResponse = ChatRoomResponse.builder()
                .chatRoomId(chatRoomService.getOrCreateDirectChatRoom(currentUserId, userId, findPartnerPostId).getId())
                .build();
        return ResponseEntity.ok(chatRoomResponse);
    }

    @PostMapping("/{roomId}/users/{userId}")
    public ResponseEntity<Boolean> addUserToGroupChatRoom(@PathVariable String roomId,
                                                         @PathVariable String userId) {
        chatRoomService.addUserToGroupChatRoom(roomId, userId);
        return ResponseEntity.ok(true);
    }

    @DeleteMapping("/{roomId}/users/{userId}")
    public ResponseEntity<Boolean> removeUserFromGroupChatRoom(@PathVariable String roomId,
                                                              @PathVariable String userId) {
        String managerId = this.getUserInfo().getId();
        chatRoomService.removeUserFromGroupChatRoom(managerId, roomId, userId);
        return ResponseEntity.ok(true);
    }

    @DeleteMapping("/{roomId}/exit")
    public ResponseEntity<Boolean> exitGroupChatRoom(@PathVariable String roomId) {
        String userId = this.getUserInfo().getId();
        chatRoomService.exitGroupChatRoom(roomId, userId);
        return ResponseEntity.ok(true);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Boolean> deleteGroupChatRoom(@PathVariable String roomId) {
        String managerId = this.getUserInfo().getId();
        chatRoomService.deleteGroupChatRoom(managerId, roomId);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/{roomId}/check-user")
    public ResponseEntity<Boolean> isUserInChatRoom(@PathVariable String roomId,
                                                   @RequestParam String userId) {
        return ResponseEntity.ok(chatRoomService.isUserInChatRoom(userId, roomId));
    }

    @PostMapping("/{roomId}/check-users")
    public ResponseEntity<Boolean> areUsersInChatRoom(@PathVariable String roomId,
                                                     @RequestBody Set<String> userIds) {
        return ResponseEntity.ok(chatRoomService.isUsersInChatRoom(userIds, roomId));
    }
}
