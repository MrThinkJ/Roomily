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
public class ChatMessageController {
    ChatMessageService chatMessageService;

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageResponse>> getChatMessages(@RequestParam String user1,
                                                               @RequestParam String user2,
                                                               @RequestParam String pivot,
                                                               @RequestParam String timestamp,
                                                               @RequestParam int prev){
        List<ChatMessageResponse> messages = chatMessageService.getChatMessages(user1, user2, pivot, timestamp, prev);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(@ModelAttribute ChatMessageToAdd chatMessageToAdd){
        ChatMessageResponse message = chatMessageService.saveChatMessage(chatMessageToAdd);
        return ResponseEntity.ok(message);
    }
}
