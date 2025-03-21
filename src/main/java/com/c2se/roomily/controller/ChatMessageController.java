package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.ChatMessageToAdd;
import com.c2se.roomily.payload.response.ChatMessageResponse;
import com.c2se.roomily.service.ChatMessageService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatMessageController extends BaseController {
    ChatMessageService chatMessageService;

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageResponse>> getChatMessages(@RequestParam String chatRoomId,
                                                                     @RequestParam(required = false) String pivot,
                                                                     @RequestParam(required = false) String timestamp,
                                                                     @RequestParam(defaultValue = "5") int prev) {
        String user1 = this.getUserInfo().getId();
        List<ChatMessageResponse> messages = chatMessageService.getChatMessages(chatRoomId, user1, pivot, timestamp, prev);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(@ModelAttribute ChatMessageToAdd chatMessageToAdd) {
        chatMessageToAdd.setSenderId(this.getUserInfo().getId());
        ChatMessageResponse message = chatMessageService.saveChatMessage(chatMessageToAdd);
        return ResponseEntity.ok(message);
    }
}
