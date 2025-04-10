package com.c2se.roomily.service.impl;

import com.c2se.roomily.event.pojo.AppEvent;
import com.c2se.roomily.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final ApplicationEventPublisher publisher;

    @Override
    public void publishEvent(AppEvent event) {
        publisher.publishEvent(event);
    }
}
