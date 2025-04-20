package com.c2se.roomily.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "roomily-exchange";
    public static final String ADS_EXCHANGE_NAME = "ads-exchange";
    public static final String ROOM_QUEUE = "room-queue";
    public static final String EVENT_QUEUE = "event-queue";
    public static final String AD_CLICK_QUEUE = "ad-click-queue";
    public static final String ROOM_ROUTING_KEY = "room-key";
    public static final String EVENT_ROUTING_KEY = "event-key";
    public static final String AD_CLICK_ROUTING_KEY = "ad-click-key";
    public static final String AD_CONVERSION_QUEUE = "ad-conversion-queue";
    public static final String AD_CONVERSION_ROUTING_KEY = "ad-conversion-key";
    
    @Value("${rabbitmq.username}")
    private String username;
    @Value("${rabbitmq.password}")
    private String password;
    @Value("${rabbitmq.host}")
    private String host;
    @Value("${rabbitmq.port}")
    private int port;

    @Bean
    ConnectionFactory rabbitMQconnectionFactory(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        return connectionFactory;
    }

    @Bean
    Queue roomQueue() {
        return new Queue(ROOM_QUEUE, false);
    }

    @Bean
    Queue eventQueue() {
        return new Queue(EVENT_QUEUE, false);
    }
    
    @Bean
    Queue adClickQueue() {
        return new Queue(AD_CLICK_QUEUE, false);
    }

    @Bean
    Queue adConversionQueue() {
        return new Queue(AD_CONVERSION_QUEUE, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }
    
    @Bean
    TopicExchange adsExchange() {
        return new TopicExchange(ADS_EXCHANGE_NAME);
    }

    @Bean
    Binding binding1(Queue roomQueue, TopicExchange exchange) {
        return BindingBuilder.bind(roomQueue).to(exchange).with(ROOM_ROUTING_KEY);
    }

    @Bean
    Binding binding2(Queue eventQueue, TopicExchange exchange) {
        return BindingBuilder.bind(eventQueue).to(exchange).with(EVENT_ROUTING_KEY);
    }
    
    @Bean
    Binding adClickBinding(Queue adClickQueue, TopicExchange adsExchange) {
        return BindingBuilder.bind(adClickQueue).to(adsExchange).with(AD_CLICK_ROUTING_KEY);
    }

    @Bean
    Binding adConversionBinding(Queue adConversionQueue, TopicExchange adsExchange) {
        return BindingBuilder.bind(adConversionQueue).to(adsExchange).with(AD_CONVERSION_ROUTING_KEY);
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory rabbitMQconnectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(rabbitMQconnectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
}
