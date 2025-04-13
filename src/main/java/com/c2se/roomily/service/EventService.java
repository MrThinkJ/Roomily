package com.c2se.roomily.service;

import com.c2se.roomily.event.pojo.AppEvent;
import org.springframework.context.ApplicationEvent;

public interface EventService {
    void publishEvent(ApplicationEvent event);
}
