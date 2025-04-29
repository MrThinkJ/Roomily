package com.c2se.roomily.service.impl;

import com.c2se.roomily.config.RabbitMQConfig;
import com.c2se.roomily.service.MessageProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQProducerService implements MessageProducerService {
    private final RabbitTemplate rabbitTemplate;
    @Override
    public void sendMessage(String message, String exchangeName, String routingKey) {
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
        log.info("Message sent: " + message);
    }
}
