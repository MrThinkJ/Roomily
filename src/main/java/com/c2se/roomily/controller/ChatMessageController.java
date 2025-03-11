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
@RequestMapping("/api/chat")
public class ChatMessageController extends BaseController {
    ChatMessageService chatMessageService;

    @PostMapping("test")
    public ResponseEntity<ChatMessageResponse> test(@ModelAttribute ChatMessageToAdd chatMessageToAdd) {
        chatMessageToAdd.setSenderId(this.getUserInfo().getId());
        ChatMessageResponse message = chatMessageService.saveTestChatMessage(chatMessageToAdd);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageResponse>> getChatMessages(@RequestParam String roomId,
                                                                     @RequestParam String pivot,
                                                                     @RequestParam String timestamp,
                                                                     @RequestParam int prev) {
        String user1 = this.getUserInfo().getId();
        List<ChatMessageResponse> messages = chatMessageService.getChatMessages(roomId, user1, pivot, timestamp, prev);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(@ModelAttribute ChatMessageToAdd chatMessageToAdd) {
        chatMessageToAdd.setSenderId(this.getUserInfo().getId());
        ChatMessageResponse message = chatMessageService.saveChatMessage(chatMessageToAdd);
        return ResponseEntity.ok(message);
    }
}
