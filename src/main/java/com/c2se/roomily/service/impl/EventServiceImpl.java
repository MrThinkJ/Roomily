package com.c2se.roomily.service.impl;

import com.c2se.roomily.event.AppEvent;
import com.c2se.roomily.service.EventService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class EventServiceImpl implements EventService {
    ApplicationEventPublisher publisher;

    @Override
    public void publishEvent(AppEvent event) {
        publisher.publishEvent(event);
    }
}
