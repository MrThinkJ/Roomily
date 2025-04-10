package com.c2se.roomily.event.handler;

import com.c2se.roomily.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQConsumer {
//    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
//    public void handle(String message) {
//        System.out.println("Handle message: " + message);
//    }
}
