package com.c2se.roomily.controller;

import com.c2se.roomily.service.MessageProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/producer")
@RequiredArgsConstructor
public class MessageProducerController {
    private final MessageProducerService messageProducerService;

    @PostMapping
    public void sendMessage(@RequestBody String message) {
        messageProducerService.sendMessage(message);
    }
}
